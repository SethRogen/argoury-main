function handleSwitch(player, interfaceId1, childId1, interfaceId2, childId2, id1, id2, indexFrom, indexTo) {
    switch(interfaceId1) {
    case 149:
        indexTo -= 28
    case 763:
        var item1 = player.getInventory().get(indexFrom)
        var item2 = player.getInventory().get(indexTo)
        if(item1 == null) {
            return
        }
        player.getInventory().set(item1, indexTo)
        player.getInventory().set(item2, indexFrom)
        if(interfaceId1 != 149) {
            player.getInventory().refresh()
        }
        break
    default:
        unhandledSwitch(player, interfaceId1, childId1, interfaceId2, childId2, id1, id2, indexFrom, indexTo);
        break
    }
}

function unhandledSwitch(player, interfaceId1, childId1, interfaceId2, childId2, id1, id2, indexFrom, indexTo) {
    logger().debug("Unhandled item switch [inter1="+interfaceId1 +", child1="+childId1+", inter2="+interfaceId2 + ", child2="+childId2 + ", id1=" + id1 + ", id2=" + id2 + ", from=" + indexFrom + ", to=" + indexTo + "]");
}