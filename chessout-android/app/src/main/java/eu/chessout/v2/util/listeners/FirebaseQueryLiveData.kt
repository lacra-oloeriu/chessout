package eu.chessout.v2.util.listeners

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.*
import eu.chessout.shared.Constants


class FirebaseQueryLiveData<T> : LiveData<T> {
    private val query: Query
    private val innerListener = MyValueEventListener()
    private val myListener: MyConverter<T>
    private var logMessages = false
    private var tag = "noTag"

    constructor(query: Query, listener: MyConverter<T>) {
        this.query = query
        this.myListener = listener
    }

    constructor(ref: DatabaseReference, listener: MyConverter<T>) {
        query = ref
        this.myListener = listener
    }

    constructor(ref: DatabaseReference, listener: MyConverter<T>, tag: String) {
        query = ref
        this.myListener = listener
        this.tag = tag
        this.logMessages = true
    }

    override fun onActive() {
        if (logMessages) {
            Log.d(Constants.LOG_TAG, "$tag onActive")
        }
        query.addValueEventListener(innerListener)
    }

    override fun onInactive() {
        if (logMessages) {
            Log.d(Constants.LOG_TAG, "$tag onInactive")
        }
        query.removeEventListener(innerListener)
    }

    inner class MyValueEventListener : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = myListener.getValue(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(
                Constants.LOG_TAG,
                "Can't listen to query $query",
                databaseError.toException()
            )
        }
    }


    interface MyConverter<T> {
        fun getValue(dataSnapshot: DataSnapshot): T
    }

}