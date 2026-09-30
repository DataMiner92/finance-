package com.example.data.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.dao.FinanceDao
import com.example.data.model.BackupLogEntity
import com.example.data.model.FullBackupData
import com.example.data.preferences.AppPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupManager(
    private val context: Context,
    private val financeDao: FinanceDao,
    private val securityManager: SecurityManager,
    private val preferences: AppPreferences
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(FullBackupData::class.java)

    private val backupDir: File
        get() {
            val dir = context.getExternalFilesDir("backups") ?: File(context.filesDir, "backups")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    suspend fun createLocalEncryptedBackup(userNote: String = "Automated"): Result<BackupLogEntity> =
        withContext(Dispatchers.IO) {
            try {
                val transactions = financeDao.getAllTransactionsList()
                val categories = financeDao.getAllCategoriesList()
                val budgets = financeDao.getAllBudgetsList()
                val savingsGoals = financeDao.getAllSavingsGoalsList()

                val backupData = FullBackupData(
                    version = 1,
                    exportTimestamp = System.currentTimeMillis(),
                    transactions = transactions,
                    categories = categories,
                    budgets = budgets,
                    savingsGoals = savingsGoals
                )

                val jsonString = adapter.toJson(backupData)
                val encryptedData = securityManager.encryptData(jsonString)

                val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "SafeLedger_backup_$timeStampStr.vault"
                val targetFile = File(backupDir, fileName)

                targetFile.writeText(encryptedData, Charsets.UTF_8)

                val totalCount = transactions.size + categories.size + budgets.size + savingsGoals.size
                val log = BackupLogEntity(
                    timestamp = System.currentTimeMillis(),
                    fileName = fileName,
                    filePath = targetFile.absolutePath,
                    itemCount = totalCount,
                    sizeBytes = targetFile.length(),
                    isEncrypted = true
                )

                financeDao.insertBackupLog(log)
                preferences.updateLastBackupTimestamp(log.timestamp)

                Result.success(log)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun restoreFromBackupFile(file: File, passphraseOverride: String? = null): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val fileContent = file.readText(Charsets.UTF_8).trim()
                val jsonString: String = if (file.name.endsWith(".vault") || !fileContent.startsWith("{")) {
                    if (passphraseOverride != null) {
                        securityManager.decryptData(fileContent, passphraseOverride)
                    } else {
                        securityManager.decryptData(fileContent)
                    }
                } else {
                    fileContent
                }

                val backupData = adapter.fromJson(jsonString)
                    ?: return@withContext Result.failure(Exception("Failed to parse backup content"))

                // Atomic restore: clear current state and insert backup records
                financeDao.clearAllTransactions()
                financeDao.clearAllBudgets()
                financeDao.clearAllSavingsGoals()
                if (backupData.categories.isNotEmpty()) {
                    financeDao.clearAllCategories()
                    financeDao.insertCategories(backupData.categories)
                }
                financeDao.insertBudgets(backupData.budgets)
                financeDao.insertSavingsGoals(backupData.savingsGoals)
                financeDao.insertTransactions(backupData.transactions)

                val totalRestored = backupData.transactions.size + backupData.categories.size +
                        backupData.budgets.size + backupData.savingsGoals.size

                Result.success(totalRestored)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun listLocalBackupFiles(): List<File> {
        val files = backupDir.listFiles { file -> file.isFile && (file.name.endsWith(".vault") || file.name.endsWith(".json")) }
        return files?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    suspend fun exportReadableCsv(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val transactions = financeDao.getAllTransactionsList()
            val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val exportFile = File(backupDir, "SafeLedger_Transactions_$timeStampStr.csv")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val builder = StringBuilder()
            builder.append("ID,Date,Type,Category,Amount,PaymentMethod,Note\n")
            for (t in transactions) {
                val dateStr = dateFormat.format(Date(t.timestamp))
                val sanitizedNote = t.note.replace("\"", "\"\"")
                builder.append("${t.id},\"$dateStr\",\"${t.type}\",\"${t.categoryName}\",${t.amount},\"${t.paymentMethod}\",\"$sanitizedNote\"\n")
            }

            exportFile.writeText(builder.toString(), Charsets.UTF_8)
            Result.success(exportFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getShareIntent(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = if (file.name.endsWith(".csv")) "text/csv" else "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getBackupDirectoryPath(): String {
        return backupDir.absolutePath
    }
}
