package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FinanceDao
import com.example.data.model.BackupLogEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseRecord
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseRecord::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        BackupLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safeledger_database.db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default categories, budgets, savings goals, initial transactions & expenses
                        scope.launch(Dispatchers.IO) {
                            val database = getDatabase(context, scope)
                            val dao = database.financeDao()
                            val expenseDao = database.expenseDao()

                            dao.insertCategories(PreloadData.defaultCategories)
                            dao.insertBudgets(PreloadData.defaultBudgets)
                            dao.insertSavingsGoals(PreloadData.defaultSavingsGoals)
                            dao.insertTransactions(PreloadData.initialTransactions())
                            expenseDao.insertExpenses(PreloadData.initialExpenseRecords())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
