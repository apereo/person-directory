import java.util.*

Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def logger = args[1]
    Map currentAttributes = args[2]

    List currentAttributeList = currentAttributes.get("current_attribute");
    def newAttribute = currentAttributeList != null && !currentAttributeList.isEmpty() ? "found_" + currentAttributeList.get(0) : null

    logger.debug("Things are happening just fine with uid: " + uid)
    def returnMap = [username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
    if (newAttribute != null)
        returnMap.put("new_attribute", [newAttribute])
    return returnMap
}

