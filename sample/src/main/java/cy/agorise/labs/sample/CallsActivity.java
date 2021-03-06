package cy.agorise.labs.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ConnectionStatusUpdate;
import cy.agorise.graphenej.api.android.RxBus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class CallsActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private static final String RECONNECT_NODE = "reconnect_node";
    private static final String TEST_BRAINKEY_DERIVATION = "test_brainkey_derivation";
    public static final String CREATE_HTLC = "create_htlc";
    public static final String REDEEM_HTLC = "redeem_htlc";

    @BindView(R.id.call_list)
    RecyclerView mRecyclerView;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);
        ButterKnife.bind(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new CallAdapter());

        Disposable disposable = RxBus.getBusInstance()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object message) throws Exception {
                        if(message instanceof ConnectionStatusUpdate){
                            ConnectionStatusUpdate statusUpdate = (ConnectionStatusUpdate) message;
                            Log.d(TAG, String.format("ConnectionStatusUpdate. code: %d, api: %d", statusUpdate.getUpdateCode(),statusUpdate.getApi()));
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    private final class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {

        private String[] supportedCalls = new String[]{
            RPC.CALL_GET_OBJECTS,
            RPC.CALL_GET_ACCOUNTS,
            RPC.CALL_GET_BLOCK,
            RPC.CALL_GET_BLOCK_HEADER,
            RPC.CALL_GET_MARKET_HISTORY,
            RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY,
            RPC.CALL_GET_REQUIRED_FEES,
            RPC.CALL_LOOKUP_ASSET_SYMBOLS,
            RPC.CALL_LIST_ASSETS,
            RPC.CALL_GET_ASSETS,
            RPC.CALL_GET_ACCOUNT_BY_NAME,
            RPC.CALL_GET_LIMIT_ORDERS,
            RPC.CALL_GET_ACCOUNT_HISTORY_BY_OPERATIONS,
            RPC.CALL_GET_FULL_ACCOUNTS,
            RPC.CALL_SET_SUBSCRIBE_CALLBACK,
            RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES,
            RPC.CALL_GET_KEY_REFERENCES,
            RPC.CALL_GET_ACCOUNT_BALANCES,
            RPC.CALL_BROADCAST_TRANSACTION,
            RPC.CALL_GET_TRANSACTION,
            RECONNECT_NODE,
            TEST_BRAINKEY_DERIVATION,
            CREATE_HTLC,
            REDEEM_HTLC
        };

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = supportedCalls[position];
            String formattedName = name.replace("_", " ").toUpperCase();
            holder.mCallNameView.setText(formattedName);
            holder.mCallNameView.setOnClickListener((view) -> {
                String selectedCall = supportedCalls[position];
                Intent intent;
                if(selectedCall.equals(RPC.CALL_SET_SUBSCRIBE_CALLBACK)){
                    intent = new Intent(CallsActivity.this, SubscriptionActivity.class);
                } else if (selectedCall.equals(RECONNECT_NODE)){
                    intent = new Intent(CallsActivity.this, RemoveNodeActivity.class);
                } else if (selectedCall.equals(TEST_BRAINKEY_DERIVATION)){
                    intent = new Intent(CallsActivity.this, BrainkeyActivity.class);
                } else if (selectedCall.equals(CREATE_HTLC) || selectedCall.equals(REDEEM_HTLC)){
                    intent = new Intent(CallsActivity.this, HtlcActivity.class);
                    intent.putExtra(Constants.KEY_SELECTED_CALL, selectedCall);
                } else {
                    intent = new Intent(CallsActivity.this, PerformCallActivity.class);
                    intent.putExtra(Constants.KEY_SELECTED_CALL, selectedCall);
                }
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return supportedCalls.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mCallNameView;

            public ViewHolder(TextView view) {
                super(view);
                this.mCallNameView = view;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
