package tech.wundero.placeholderredux.api.data;

import org.spongepowered.api.text.Text;
import tech.wundero.placeholderredux.api.function.FunctionArg;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface PlaceholderData {

    // info metadata

    String placeholderID();

    String description();

    List<String> authors();

    Optional<String> url();

    List<Text> helpText();

    // parsing metadata

    Map<String, FunctionArg<?>> expectedArgumentTypes();


    /**
     * Map [argument value -> next argument names (ordered)]
     * If the function, given 'a', returns ['b', 'c', 'd'],
     * the expected parse order is:
     * - Start at element 0
     *  - If element is a key in {@link PlaceholderData#expectedArgumentTypes()}
     *   - If element's FunctionArg from {@link PlaceholderData#expectedArgumentTypes()}'s
     *     {@link FunctionArg#isValid(Object)} returns true for the parsed argument
     *    - The element is selected, the rest are ignored
     * - Move onto element 1, repeat step 1
     * @return A function which maps argument values to a list of ordered argument names which can follow the given arg
     */
    Function<String, List<String>> argumentSequence();

}
