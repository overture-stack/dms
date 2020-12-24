package bio.overture.dms.rest.okhttp;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

// TODO: remove this, as not doing anything
@Deprecated
@Slf4j
public class OkHttpResponseCallback implements Callback {
  public static final OkHttpResponseCallback INSTANCE = new OkHttpResponseCallback();

  @Override
  public void onFailure(@NotNull Call call, @NotNull IOException e) {
    log.error(e.getMessage());
  }

  @Override
  public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
    if (!response.isSuccessful()) {
      //      onFailure(call, new OkHttpException(response));
    }
  }
}
