package chris.seProxy.proxy;

import java.util.Map;
import java.util.Optional;

public class Utils {

    public static <K, V> Optional<V> optionalGet(Map<K, V> map, K key) {
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(key));
    }
}
