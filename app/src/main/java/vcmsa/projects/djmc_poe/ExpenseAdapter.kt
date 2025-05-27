package vcmsa.projects.djmc_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExpenseAdapter(
    private var expenseList: List<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvExpenseTitle)
        val amount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        val dateTime: TextView = itemView.findViewById(R.id.tvExpenseDateTime)
        val category: TextView = itemView.findViewById(R.id.tvExpenseCategory)
        val image: ImageView = itemView.findViewById(R.id.ivExpenseImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.title.text = expense.title
        holder.amount.text = "R${String.format("%.2f", expense.amount)}"
        holder.dateTime.text = expense.date
        holder.category.text = expense.category

        if (!expense.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(expense.imageUrl)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder)
        }
    }

    override fun getItemCount(): Int = expenseList.size

    fun updateList(newList: List<Expense>) {
        expenseList = newList
        notifyDataSetChanged()
    }
}
