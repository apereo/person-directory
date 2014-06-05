import java.util.List;
import java.util.Map;

class SampleGroovyPersonAttributeDao {
    def Map<String, List<Object>> run(final Object... args) {
        
        def uid = args[0]
        def logger = args[1];
        
        logger.debug("[{}]: The received uid is {}", this.class.simpleName, uid)
        return[name:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    }
}