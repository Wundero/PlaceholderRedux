package tech.wundero.placeholderredux.util;

import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import tech.wundero.placeholderredux.PlaceholderRedux;
import tech.wundero.placeholderredux.api.data.PlaceholderData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class Utils {

    public static final ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

    public static <T> CompletableFuture<T> returnAsync(Supplier<T> src) {
        CompletableFuture<T> future = new CompletableFuture<>();
//        Task.builder().async().execute(() -> {
//            future.complete(src.get());
//        }).submit(PlaceholderRedux.get());
        service.execute(() -> {
            try {
                future.complete(src.get());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static List<Text> genHelp(PlaceholderData data) {
        return new ArrayList<>();
    }

}
