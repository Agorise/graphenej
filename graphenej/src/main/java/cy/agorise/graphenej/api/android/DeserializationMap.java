package cy.agorise.graphenej.api.android;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;

import cy.agorise.graphenej.AccountOptions;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.AssetOptions;
import cy.agorise.graphenej.Authority;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.api.calls.GetAccountByName;
import cy.agorise.graphenej.api.calls.GetAccounts;
import cy.agorise.graphenej.api.calls.GetBlock;
import cy.agorise.graphenej.api.calls.GetBlockHeader;
import cy.agorise.graphenej.api.calls.GetMarketHistory;
import cy.agorise.graphenej.api.calls.GetObjects;
import cy.agorise.graphenej.api.calls.GetRelativeAccountHistory;
import cy.agorise.graphenej.api.calls.GetRequiredFees;
import cy.agorise.graphenej.api.calls.ListAssets;
import cy.agorise.graphenej.api.calls.LookupAssetSymbols;
import cy.agorise.graphenej.models.AccountProperties;
import cy.agorise.graphenej.models.Block;
import cy.agorise.graphenej.models.BlockHeader;
import cy.agorise.graphenej.models.BucketObject;
import cy.agorise.graphenej.models.OperationHistory;
import cy.agorise.graphenej.objects.Memo;
import cy.agorise.graphenej.operations.CustomOperation;
import cy.agorise.graphenej.operations.LimitOrderCreateOperation;
import cy.agorise.graphenej.operations.TransferOperation;

/**
 * Class used to store a mapping of request class to two important things:
 *
 * 1- The class to which the corresponding response should be de-serialized to
 * 2- An instance of the Gson class, with all required type adapters
 */
public class DeserializationMap {
    private final String TAG = this.getClass().getName();

    private HashMap<Class, Class> mClassMap = new HashMap<>();

    private HashMap<Class, Gson> mGsonMap = new HashMap<>();

    public DeserializationMap(){
        Gson genericGson = new Gson();

        // GetBlock
        mClassMap.put(GetBlock.class, Block.class);
        Gson getBlockGson = new GsonBuilder()
                .registerTypeAdapter(Transaction.class, new Transaction.TransactionDeserializer())
                .registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer())
                .registerTypeAdapter(LimitOrderCreateOperation.class, new LimitOrderCreateOperation.LimitOrderCreateDeserializer())
                .registerTypeAdapter(CustomOperation.class, new CustomOperation.CustomOperationDeserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .create();
        mGsonMap.put(GetBlock.class, getBlockGson);

        // GetAccounts
        mClassMap.put(GetAccounts.class, List.class);
        Gson getAccountsGson = new GsonBuilder()
                .setExclusionStrategies(new SkipAccountOptionsStrategy())
                .registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer())
                .registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer())
                .create();
        mGsonMap.put(GetAccounts.class, getAccountsGson);

        // GetRequiredFees
        mClassMap.put(GetRequiredFees.class, List.class);
        Gson getRequiredFeesGson = new GsonBuilder()
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .create();
        mGsonMap.put(GetRequiredFees.class, getRequiredFeesGson);

        // GetRelativeAccountHistory
        mClassMap.put(GetRelativeAccountHistory.class, List.class);
        Gson getRelativeAcountHistoryGson = new GsonBuilder()
            .setExclusionStrategies(new SkipAccountOptionsStrategy(), new SkipAssetOptionsStrategy())
            .registerTypeAdapter(OperationHistory.class, new OperationHistory.OperationHistoryDeserializer())
            .registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer())
            .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
            .registerTypeAdapter(Memo.class, new Memo.MemoDeserializer())
            .create();
        mGsonMap.put(GetRelativeAccountHistory.class, getRelativeAcountHistoryGson);

        // GetBlockHeader
        mClassMap.put(GetBlockHeader.class, BlockHeader.class);
        mGsonMap.put(GetBlockHeader.class, genericGson);

        // GetMarketHistory
        mClassMap.put(GetMarketHistory.class, List.class);
        Gson getMarketHistoryGson = new GsonBuilder()
            .registerTypeAdapter(BucketObject.class, new BucketObject.BucketDeserializer())
            .create();
        mGsonMap.put(GetMarketHistory.class, getMarketHistoryGson);

        // LookupAssetSymbols
        mClassMap.put(LookupAssetSymbols.class, List.class);
        Gson lookupAssetSymbolGson = new GsonBuilder()
                .registerTypeAdapter(Asset.class, new Asset.AssetDeserializer())
                .create();
        mGsonMap.put(LookupAssetSymbols.class, lookupAssetSymbolGson);

        // GetObjects
        mClassMap.put(GetObjects.class, List.class);
        Gson getObjectsGson = new GsonBuilder()
                .registerTypeAdapter(Asset.class, new Asset.AssetDeserializer())
                .create();
        mGsonMap.put(GetObjects.class, getObjectsGson);

        // ListAssets
        mClassMap.put(ListAssets.class, List.class);
        Gson listAssetsGson = new GsonBuilder()
            .registerTypeAdapter(Asset.class, new Asset.AssetDeserializer())
            .create();
        mGsonMap.put(ListAssets.class, listAssetsGson);

        // GetAccountByName
        mClassMap.put(GetAccountByName.class, AccountProperties.class);
        Gson getAccountByNameGson = new GsonBuilder()
            .registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer())
            .registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer())
            .create();
        mGsonMap.put(GetAccountByName.class, getAccountByNameGson);
    }

    public Class getReceivedClass(Class _class){
        return mClassMap.get(_class);
    }

    public Gson getGson(Class aClass) {
        return mGsonMap.get(aClass);
    }

    /**
     * This class is required in order to break a recursion loop when de-serializing the
     * AccountProperties class instance.
     */
    public static class SkipAccountOptionsStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == AccountOptions.class;
        }
    }

    /**
     * This class is required in order to break a recursion loop when de-serializing the
     * AssetAmount instance.
     */
    public static class SkipAssetOptionsStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == AssetOptions.class;
        }
    }
}
