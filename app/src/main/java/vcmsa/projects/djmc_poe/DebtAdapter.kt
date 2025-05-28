package vcmsa.projects.djmc_poe

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import vcmsa.projects.djmc_poe.Debt

class DebtAdapter(
    private val context: Context,
    private val debtList: MutableList<Debt>,
    private val onDeleteClick: (Debt, Int) -> Unit
) : RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {

    inner class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val debtName: TextView = itemView.findViewById(R.id.tvDebtName)
        val debtAmount: TextView = itemView.findViewById(R.id.tvDebtAmount)
        val interestAmount: TextView = itemView.findViewById(R.id.tvInterestAmount)
        val months: TextView = itemView.findViewById(R.id.tvMonths)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val paymentEntrybutton = itemView.findViewById<Button>(R.id.PaymentEntrybtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_debt, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = debtList[position]
        holder.debtName.text = debt.debtname
        holder.debtAmount.text = "Amount: R${debt.debtamt}"
        holder.interestAmount.text = "Interest: ${debt.interestamt}%"
        holder.months.text = "Months: ${debt.npmonthsperiod}"
        holder.category.text = "Category: ${debt.category}"

        holder.btnDelete.setOnClickListener {
            onDeleteClick(debt, position)
        }

//        holder.paymentEntrybutton.setOnClickListener {
//            val intent = Intent(context, PaymentEntryActivity::class.java)
//            context.startActivity(intent)
//        }

        holder.paymentEntrybutton.setOnClickListener {
            val intent = Intent(context, PaymentEntryActivity::class.java)
            intent.putExtra("debtId", debt.documentId)
            intent.putExtra("debtName", debt.debtname)
            intent.putExtra("debtAmount", debt.debtamt)
            context.startActivity(intent)
        }


    }

    override fun getItemCount() = debtList.size

    fun removeDebt(position: Int) {
        debtList.removeAt(position)
        notifyItemRemoved(position)
    }
}


//class DebtAdapter(private val debtList: List<Debt>) :
//    RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {
//
//    class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val debtName: TextView = itemView.findViewById(R.id.tvDebtName)
//        val debtAmount: TextView = itemView.findViewById(R.id.tvDebtAmount)
//        val interestAmount: TextView = itemView.findViewById(R.id.tvInterestAmount)
//        val months: TextView = itemView.findViewById(R.id.tvMonths)
//        val category: TextView = itemView.findViewById(R.id.tvCategory)
//        val pieChart: PieChart = itemView.findViewById(R.id.pieChart)
//
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_debt, parent, false) // Make sure this matches your RecyclerView item layout filename
//        return DebtViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
//        val debt = debtList[position]
//        holder.debtName.text = debt.debtname
//        holder.debtAmount.text = "Amount: R${debt.debtamt}"
//        holder.interestAmount.text = "Interest: ${debt.interestamt}%"
//        holder.months.text = "Months: ${debt.npmonthsperiod}"
//        holder.category.text = "Category: ${debt.category}"
//
//
////        val entries = ArrayList<PieEntry>()
////        entries.add(PieEntry(debt.debtamt.toFloat(), "Principal"))
////        entries.add(PieEntry(debt.interestamt.toFloat(), "Interest"))
////
////        val dataSet = PieDataSet(entries, "Debt Breakdown")
////        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
////        dataSet.valueTextSize = 14f
////
////        val data = PieData(dataSet)
////        holder.pieChart.data = data
////        holder.pieChart.description.isEnabled = false
////        holder.pieChart.centerText = "Debt Split"
////        holder.pieChart.animateY(500)
////        holder.pieChart.invalidate()
//
//    }
//
//    override fun getItemCount() = debtList.size
//}
