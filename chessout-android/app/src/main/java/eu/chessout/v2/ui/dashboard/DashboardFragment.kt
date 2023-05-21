package eu.chessout.v2.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment() {

    private lateinit var myViewModel: MyViewModel
    private val myListAdapter = MyAdapter(arrayListOf())

    private val myObserver = Observer<List<DashboardModel>> { list ->
        list?.let {
            my_recycler_view.visibility = View.VISIBLE
            myListAdapter.updateArrayList(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myViewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        myViewModel.dashboardModelList.observe(viewLifecycleOwner, myObserver)
        myViewModel.getInitialList()

        my_recycler_view?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = myListAdapter
        }
    }
}