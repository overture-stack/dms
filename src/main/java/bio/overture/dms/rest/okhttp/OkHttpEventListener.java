package bio.overture.dms.rest.okhttp;

import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class OkHttpEventListener extends EventListener {

  @NonNull private final Callback callback;

  @Override
  public void responseFailed(@NotNull Call call, @NotNull IOException ioe) {
    call.enqueue(callback);
    super.responseFailed(call, ioe);
  }
}
