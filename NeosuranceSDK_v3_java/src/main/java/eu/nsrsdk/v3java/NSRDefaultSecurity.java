package eu.nsrsdk.v3java;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import eu.nsrsdk.utils.NSRUtils;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NSRDefaultSecurity implements NSRSecurityDelegate {

	private static AsynchRequestCustom asynchRequestCustom = null;
	private static HandlerThread nsrNetworkingHandlerThread = null;

	public void secureRequest(final Context ctx, final String endpoint, final JSONObject payload, final JSONObject headers, final NSRSecurityResponse completionHandler) throws Exception {
		try {
			String url = NSRUtils.getSettings(ctx).getString("base_url") + endpoint;
			NSRLog.d("NSRDefaultSecurity: " + url);

			if(asynchRequestCustom == null)
				asynchRequestCustom = new AsynchRequestCustom(nsrNetworkingHandlerThread);

			if(nsrNetworkingHandlerThread == null)
				nsrNetworkingHandlerThread = new HandlerThread("nsrNetworkingHandlerThread");

			asynchRequestCustom.doAsyncTask(url, payload, headers, completionHandler);

			//AsynchRequest asynchRequest = new AsynchRequest(url, payload, headers, completionHandler);
			//asynchRequest.execute();
		} catch (Exception e) {
			NSRLog.e(e.getMessage());
			throw e;
		}
	}

	private class AsynchRequestCustom{

		public String url;
		public JSONObject payload;
		public JSONObject headers;
		public NSRSecurityResponse completionHandler;

		public HandlerThread nsrNHandlerThread;

		public AsynchRequestCustom(HandlerThread nsrNetworkingHandlerThread) {
			this.nsrNHandlerThread = nsrNetworkingHandlerThread;
		}

		public void doAsyncTask(final String url, final JSONObject payload, final JSONObject headers, final NSRSecurityResponse completionHandler) {

			this.url = url;
			this.payload = payload;
			this.headers = headers;
			this.completionHandler = completionHandler;

			Handler asyncHandler = new Handler(this.nsrNHandlerThread.getLooper());

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					// your async code goes here.

					NSRHttpRunner httpRunner = null;

					try {

						httpRunner = new NSRHttpRunner(url);

						if (payload != null)
							httpRunner.payload(payload.toString(), "application/json");

						if (headers != null) {
							Iterator<String> keys = headers.keys();
							while (keys.hasNext()) {
								String key = keys.next();
								httpRunner.header(key, headers.getString(key));
							}
						}
						String response = httpRunner.read();
						NSRLog.d("NSRDefaultSecurity response:" + response);

						completionHandler.completionHandler(new JSONObject(response), null);

					} catch (Exception e) {
						try {
							if (httpRunner != null) {
								NSRLog.e("MSG:" + httpRunner.getMessage());
								NSRLog.e("Error:" + e.getMessage());
							}
							completionHandler.completionHandler(null, e.toString());
						} catch (Exception ee) {
							NSRLog.e(ee.toString());
						}
					}

				}
			};

			asyncHandler.post(runnable);

		}

	}

	/*
	* AsyncTask was intended to enable proper and easy use of the UI thread. However, the most
 	* common use case was for integrating into UI, and that would cause Context leaks, missed
 	* callbacks, or crashes on configuration changes. It also has inconsistent behavior on different
 	* versions of the platform, swallows exceptions from {@code doInBackground}, and does not provide
 	* much utility over using {@link Executor}s directly
	*/
	private class AsynchRequest extends AsyncTask<Void, Void, Object> {
		private String url;
		private JSONObject payload;
		private JSONObject headers;
		private NSRSecurityResponse completionHandler;

		public AsynchRequest(final String url, final JSONObject payload, final JSONObject headers, final NSRSecurityResponse completionHandler) {
			this.url = url;
			this.payload = payload;
			this.headers = headers;
			this.completionHandler = completionHandler;
		}

		protected Object doInBackground(Void... params) {
			NSRHttpRunner httpRunner = null;
			try {
				httpRunner = new NSRHttpRunner(url);
				if (payload != null)
					httpRunner.payload(payload.toString(), "application/json");

				if (headers != null) {
					Iterator<String> keys = headers.keys();
					while (keys.hasNext()) {
						String key = keys.next();
						httpRunner.header(key, headers.getString(key));
					}
				}
				String response = httpRunner.read();
				NSRLog.d("NSRDefaultSecurity response:" + response);
				completionHandler.completionHandler(new JSONObject(response), null);
			} catch (Exception e) {
				try {
					if (httpRunner != null) {
						NSRLog.e("MSG:" + httpRunner.getMessage());
						NSRLog.e("Error:" + e.getMessage());
					}
					completionHandler.completionHandler(null, e.toString());
				} catch (Exception ee) {
					NSRLog.e(ee.toString());
				}
			}
			return null;
		}
	}
}