package tech.wundero.placeholderredux.api.format;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.annotation.CatalogedBy;
import tech.wundero.placeholderredux.api.PlaceholderTemplate;
import tech.wundero.placeholderredux.format.PlaceholderFormats;
import tech.wundero.placeholderredux.util.Utils;

import java.util.concurrent.CompletableFuture;

@CatalogedBy(PlaceholderFormats.class)
public interface PlaceholderFormat extends CatalogType {

    PlaceholderTemplate parse(String string);

    PlaceholderTemplate parse(Text text);

    PlaceholderTemplate parse(TextTemplate template);

//    default CompletableFuture<PlaceholderTemplate> parseAsync(String string) {
//        return Utils.returnAsync(() -> parse(string));
//    }
//
//    default CompletableFuture<PlaceholderTemplate> parseAsync(Text text) {
//        return Utils.returnAsync(() -> parse(text));
//    }
//
//    default CompletableFuture<PlaceholderTemplate> parseAsync(TextTemplate template) {
//        return Utils.returnAsync(() -> parse(template));
//    }
}
