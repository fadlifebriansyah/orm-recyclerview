package test.fadli.com.netcache.realm;

import io.realm.RealmResults;
import test.fadli.com.netcache.model.DataModel;

public class RealmDataAdapter extends RealmModelAdapter<DataModel> {

    public RealmDataAdapter(RealmResults<DataModel> realmResults) {

        super(realmResults);
    }
}
