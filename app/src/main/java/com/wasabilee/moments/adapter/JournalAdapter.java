package com.wasabilee.moments.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.wasabilee.moments.data.models.DateData;
import com.wasabilee.moments.data.models.Journal;
import com.wasabilee.moments.data.models.JournalData;
import com.wasabilee.moments.data.models.JournalListDisplayItem;
import com.wasabilee.moments.R;
import com.wasabilee.moments.utils.JournalItemActionsListener;
import com.wasabilee.moments.viewmodel.MainViewModel;
import com.wasabilee.moments.databinding.ItemJournalHeaderBinding;
import com.wasabilee.moments.databinding.ItemJournalItemBinding;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {

    private static final String TAG = JournalAdapter.class.getSimpleName();

    private MainViewModel mViewModel;
    private List<JournalData> mJournals;
    private JournalItemActionsListener mListener;


    public JournalAdapter(List<JournalData> journals, MainViewModel viewModel) {
        this.mViewModel = viewModel;
        this.mListener = journalId -> mViewModel.getmOpenJournalDetail().setValue(journalId);
        setList(journals);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemJournalHeaderBinding headerBinding;
        private ItemJournalItemBinding itemBinding;

        ViewHolder(ItemJournalHeaderBinding binding) {
            super(binding.getRoot());
            headerBinding = binding;
        }

        ViewHolder(ItemJournalItemBinding binding) {
            super(binding.getRoot());
            itemBinding = binding;
        }

        ItemJournalHeaderBinding getHeaderBinding() {
            return headerBinding;
        }

        ItemJournalItemBinding getItemBinding() {
            return itemBinding;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mJournals.get(position).getType();
    }

    private void setList(List<JournalData> journals) {
        mJournals = journals;
        Log.d(TAG, "setList: " + (journals == null ? "null" : journals.size()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case JournalData.TYPE_DATE:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_journal_header, parent, false);
                return new ViewHolder((ItemJournalHeaderBinding) binding);
            case JournalData.TYPE_JOURNAL:
                binding = DataBindingUtil.inflate(inflater, R.layout.item_journal_item, parent, false);
                return new ViewHolder((ItemJournalItemBinding) binding);
        }
        Log.d(TAG, "onCreateViewHolder: returning null");
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalData currentItem = mJournals.get(position);

        switch (holder.getItemViewType()) {

            case JournalData.TYPE_DATE:
                ItemJournalHeaderBinding headerBinding = holder.getHeaderBinding();
                headerBinding.setDate((DateData) currentItem);
                headerBinding.executePendingBindings();
                break;

            case JournalData.TYPE_JOURNAL:
                ItemJournalItemBinding itemBinding = holder.getItemBinding();
                itemBinding.setJournal((Journal) currentItem);
                itemBinding.setListener(mListener);
                itemBinding.setJournalDisplayItem(new JournalListDisplayItem((Journal) currentItem));
                itemBinding.executePendingBindings();
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mJournals != null ? mJournals.size() : 0;
    }

    public void replaceData(List<JournalData> journal) {
        setList(journal);
    }
}
