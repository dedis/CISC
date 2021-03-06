package ch.epfl.dedis.api;

import ch.epfl.dedis.cisc.Activity;
import ch.epfl.dedis.cisc.R;
import ch.epfl.dedis.crypto.Utils;
import ch.epfl.dedis.net.HTTP;
import ch.epfl.dedis.net.Identity;
import ch.epfl.dedis.net.UpdateChain;
import com.google.gson.annotations.SerializedName;

/**
 * In order to verify the integrity of an entire Skipchain it has
 * to be requested from Cothority by sending a GetUpdateChain JSON.
 *
 * @author Andrea Caforio
 */
public class GetUpdateChain implements Request {

    private static final String PATH = "guc";

    private final Activity mActivity;
    private final Identity mIdentity;

    public GetUpdateChain(Activity activity, Identity identity) {
        this(activity, identity, false);
    }

    public GetUpdateChain(Activity activity, Identity identity, boolean wait) {
        mActivity = activity;
        mIdentity = identity;

        GetUpdateChainMessage getUpdateChainMessage = new GetUpdateChainMessage();
        getUpdateChainMessage.latestId = Utils.encodeBase64(mIdentity.getId());

        HTTP http = new HTTP(this, identity.getCothority(), PATH, Utils.toJson(getUpdateChainMessage));
        if (wait) {
            String result = http.doInBackground();
            http.onPostExecute(result);
        } else {
            http.execute();
        }
    }

    /**
     * When successfully received Skipchain is parsed into an UpdateChain
     * object which then is used in the three-step verification procedure.
     *
     * @param result response String from the Cothority
     */
    public void callback(String result) {
        try {
            UpdateChain uc = Utils.fromJson(result, UpdateChain.class);
            boolean verified = uc.verifySkipChain();
            if (verified) {
                mActivity.taskJoin();
            } else {
                mActivity.taskFail(R.string.info_noverification);
            }
        } catch (Exception e) {
            mActivity.taskFail(R.string.info_corruptedjson);
        }
    }

    public void callbackError(int error) {
        switch (error) {
            case 400: mActivity.taskFail(R.string.err_400); break;
            case 500: mActivity.taskFail(R.string.err_500); break;
            case 501: mActivity.taskFail(R.string.err_501); break;
            case 502: mActivity.taskFail(R.string.err_502); break;
            case 503: mActivity.taskFail(R.string.err_503); break;
            case 504: mActivity.taskFail(R.string.err_504); break;
            default: mActivity.taskFail(R.string.err_unknown);
        }
    }

    /**
     * A complete Skipchain is received by solely sending its genesis
     * identity hash.
     *
     * @author Andrea Caforio
     */
    private class GetUpdateChainMessage {
        @SerializedName("LatestID")
        String latestId;
    }
}
