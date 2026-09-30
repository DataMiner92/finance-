package com.example.data.model

data class FinancialTip(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "Budgeting", "Emergency", "Credit & Loans", "Small Business", "Savings"
    val readTimeMinutes: Int,
    val keyTakeaway: String,
    val contentSections: List<TipSection>,
    val actionStep: String
)

data class TipSection(
    val heading: String,
    val body: String
)

object FinancialTipsRepository {
    val tips = listOf(
        FinancialTip(
            id = "tip_50_30_20",
            title = "The 50/30/20 Budgeting Rule",
            subtitle = "A simple benchmark to divide your earnings",
            category = "Budgeting",
            readTimeMinutes = 2,
            keyTakeaway = "Direct 50% to essential survival Needs, 30% to comfort Wants, and at least 20% into Savings or debt clearing.",
            contentSections = listOf(
                TipSection(
                    "50% for Essentials (Needs)",
                    "These are things you cannot live or work without: food, shelter/rent, water, electricity/kerosene, vital transport to your job, and basic healthcare."
                ),
                TipSection(
                    "30% for Flexible Lifestyle (Wants)",
                    "Non-essential items: dining out, entertainment, stylish clothes beyond basic wear, and gadgets. If money is tight, this is the first bucket to cut down."
                ),
                TipSection(
                    "20% for Future Security (Savings & Debt)",
                    "Emergency reserves, debt payoff, or long-term investments like land, education, or retirement. Even if you can only manage 10% today, starting now builds momentum."
                )
            ),
            actionStep = "Use the interactive 50/30/20 simulator below to check how your monthly earnings fit this breakdown."
        ),
        FinancialTip(
            id = "tip_emergency_buffer",
            title = "Building a Local Emergency Buffer",
            subtitle = "Your shield against unexpected life shocks",
            category = "Emergency",
            readTimeMinutes = 3,
            keyTakeaway = "Having even 1 to 2 months of basic expenses safely put aside prevents selling vital assets at a loss.",
            contentSections = listOf(
                TipSection(
                    "Why Traditional Banks Aren't Mandatory",
                    "In areas with few bank branches, keeping an emergency fund in a dedicated mobile money wallet or a secure local cash box works effectively. The key rule: keep it separate from everyday spending money."
                ),
                TipSection(
                    "Start with Mini Milestones",
                    "Don't get overwhelmed aiming for a huge sum immediately. Aim first for 1 week of food and basic utility costs. Once achieved, stretch to 2 weeks, then 1 month."
                ),
                TipSection(
                    "When to Touch It",
                    "Emergency funds are exclusively for unforeseen critical events: sudden hospital clinics, essential repairs to business tools, or sudden family crises. Never for holidays or celebrations."
                )
            ),
            actionStep = "Set up an 'Emergency Buffer Fund' in the Savings Goals tab and commit a small deposit each week."
        ),
        FinancialTip(
            id = "tip_loan_apps",
            title = "Beware of Predatory Quick-Loan Apps",
            subtitle = "Recognizing short-term credit traps",
            category = "Credit & Loans",
            readTimeMinutes = 3,
            keyTakeaway = "A 10% interest rate for 14 days equals an annual interest rate exceeding 260%. Avoid borrowing to cover daily living expenses.",
            contentSections = listOf(
                TipSection(
                    "The Hidden Compounding Cost",
                    "Instant digital loans on smartphones often seem convenient, but late fees and 7-day to 30-day rollovers quickly trap borrowers in a vicious cycle of borrowing from one app to pay another."
                ),
                TipSection(
                    "Rule of Productive Borrowing",
                    "Only borrow when the loan generates clear, predictable profit (e.g. purchasing fast-moving wholesale stock where profit comfortably exceeds the loan interest). Never borrow for consumption."
                )
            ),
            actionStep = "List any current loans in your Budgets under 'Debt Repayment' to prioritize clearing the highest-interest ones first."
        ),
        FinancialTip(
            id = "tip_irregular_income",
            title = "Managing Irregular & Harvest Incomes",
            subtitle = "Surviving lean months after harvest or busy seasons",
            category = "Small Business",
            readTimeMinutes = 3,
            keyTakeaway = "Pay yourself a steady 'Base Salary' rather than spending the windfall all at once.",
            contentSections = listOf(
                TipSection(
                    "The Boom & Bust Cycle",
                    "Farmers, seasonal traders, and freelancers often experience huge influxes during peak harvest or holidays, followed by 3-4 months of near zero income."
                ),
                TipSection(
                    "The Hill-and-Valley Reservoir",
                    "When big money comes in, calculate your bare minimum monthly household cost. Multiply it by the number of lean months ahead. Lock that reservoir away and withdraw only that monthly amount."
                )
            ),
            actionStep = "Track your average 3-month expense in SafeLedger to calculate your exact monthly survival baseline."
        ),
        FinancialTip(
            id = "tip_separate_business_cash",
            title = "Separate Business from Personal Money",
            subtitle = "The #1 survival rule for shopkeepers and small trades",
            category = "Small Business",
            readTimeMinutes = 2,
            keyTakeaway = "Taking cash straight from the till for family groceries will silently bankrupt an otherwise profitable trade.",
            contentSections = listOf(
                TipSection(
                    "Two Wallets, Two Books",
                    "Keep two physical pouches or two accounts: one exclusively for business inventory restocking and revenue, and one for home household living."
                ),
                TipSection(
                    "Pay Yourself Once A Week",
                    "Decide a fixed wage for your personal work. Take that wage once a week or month. All other revenue must stay in the business to preserve working capital."
                )
            ),
            actionStep = "Categorize business stock purchases under 'Farm & Business Ops' and home food under 'Food & Groceries'."
        ),
        FinancialTip(
            id = "tip_cooling_rule",
            title = "The 24-Hour Purchase Pause",
            subtitle = "Crush impulse spending without feeling deprived",
            category = "Savings",
            readTimeMinutes = 2,
            keyTakeaway = "Wait 24 hours before buying anything that is not a basic necessity.",
            contentSections = listOf(
                TipSection(
                    "Emotional Spending",
                    "Urges to buy nice clothes, gadgets, or extra treats are strongest in the moment. Waiting 24 hours allows emotional adrenaline to subside so logic can take over."
                ),
                TipSection(
                    "The Cash Test",
                    "If you wouldn't feel happy handing over crisp paper bills for it today, you don't really need it."
                )
            ),
            actionStep = "When tempted by an unplanned expense, log it as a draft note in SafeLedger and revisit it tomorrow."
        )
    )
}
