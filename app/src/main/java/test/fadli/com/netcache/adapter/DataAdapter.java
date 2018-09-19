package test.fadli.com.netcache.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.realm.Realm;
import test.fadli.com.netcache.DetailActivity;
import test.fadli.com.netcache.R;
import test.fadli.com.netcache.model.DataModel;
import test.fadli.com.netcache.realm.RealmRecyclerViewAdapter;

public class DataAdapter extends RealmRecyclerViewAdapter<DataModel> {

    final Context context;
    private Realm realm;
    private LayoutInflater inflater;

    public DataAdapter(Context context) {
        this.context = context;
    }

    // create new views (invoked by the layout manager)
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a new card view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final DataModel datas = getItem(position);
        final CardViewHolder holder = (CardViewHolder) viewHolder;

        holder.tvTitle.setText(datas.getTitle());
        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("title", "" + datas.getTitle());
                i.putExtra("body", "" + datas.getBody());
                context.startActivity(i);
            }
        });
    }

    // return the size of your data set (invoked by the layout manager)
    public int getItemCount() {

        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;
        public RelativeLayout rl;

        public CardViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            rl = (RelativeLayout) itemView.findViewById(R.id.rl);
        }
    }
}