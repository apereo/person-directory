function run(args) {
    var uid = args[0]
    var logger = args[1]
    print("Things are happening just fine")

    var map = {};
    map["username"] = uid;
    map["likes"] = "chees";
    map["id"] = [1234,2,3,4,5];
    map["another"] = "attribute";

    return map;
}
