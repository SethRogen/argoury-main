package com.runescape.content.shop;

import java.util.List;

import com.runescape.logic.item.PossesedItem;

/**
 * @author Lazaro
 */
public class ShopDefinition {
    public String name = null;
    public PossesedItem[] sampleStock = null;
    public PossesedItem[] stock = null;
    public List<Integer> acceptedItems = null;
}
