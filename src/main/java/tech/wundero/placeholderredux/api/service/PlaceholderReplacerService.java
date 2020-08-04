package tech.wundero.placeholderredux.api.service;

import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import tech.wundero.placeholderredux.PlaceholderRedux;
import tech.wundero.placeholderredux.api.format.PlaceholderFormat;
import tech.wundero.placeholderredux.util.Utils;

import java.util.concurrent.CompletableFuture;

public interface PlaceholderReplacerService {

    default Text replace(TextTemplate template, Object source, Object target, PlaceholderFormat format) {
        return format.parse(template).apply(source, target);
    }

    default Text replace(Text text, Object source, Object target, PlaceholderFormat format) {
        return format.parse(text).apply(source, target);
    }

    default Text replace(String text, Object source, Object target, PlaceholderFormat format) {
        return format.parse(text).apply(source, target);
    }

}
