package cn.modificator.launcher.modelController;

import java.text.Collator;
import java.util.Comparator;

public class LauncherItemInfoComparator implements Comparator<LauncherItemInfo> {
  public static final int SORT_MODE_MASK = 0b111 << 3;
  public static final int SORT_MODE_ALPHABETICAL = 1 << 3;
  public static final int SORT_MODE_FIRST_APPEAR = 3 << 3;
  public static final int SORT_MODE_CUSTOM_WITH_FIRST_APPEAR = 4 << 3;
  public static final int SORT_MODE_CUSTOM_WITH_ALPHABETICAL = 5 << 3;
  public static final int SORT_ORDER_MASK = 0b111;
  public static final int SORT_ORDER_ASC = 1;
  public static final int SORT_ORDER_DESC = 2;

  private int flags;

  public LauncherItemInfoComparator(int flags) {
    this.flags = flags;
  }

  @Override
  public int compare(LauncherItemInfo itemInfo1, LauncherItemInfo itemInfo2) {
    int special1 = -1, special2 = -1;
    if (itemInfo1.id.equals(ItemCenter.WIFI_ITEM_ID)) {
      special1 = 1;
    } else if (itemInfo1.id.equals(ItemCenter.ONE_KEY_LOCK_ITEM_ID)) {
      special1 = 2;
    } else if (itemInfo1.id.equals(ItemCenter.BRIGHTNESS_ITEM_ID)) {
      special1 = 3;
    } else if (itemInfo1.id.equals(ItemCenter.CONTRAST_ITEM_ID)) {
      special1 = 4;
    }

    if (itemInfo2.id.equals(ItemCenter.WIFI_ITEM_ID)) {
      special2 = 1;
    } else if (itemInfo2.id.equals(ItemCenter.ONE_KEY_LOCK_ITEM_ID)) {
      special2 = 2;
    } else if (itemInfo2.id.equals(ItemCenter.BRIGHTNESS_ITEM_ID)) {
      special2 = 3;
    } else if (itemInfo2.id.equals(ItemCenter.CONTRAST_ITEM_ID)) {
      special2 = 4;
    }

    if (special1 > 0 || special2 > 0) {
      return special1 - special2;
    }

    int ascendingResult;
    switch (flags & SORT_MODE_MASK) {
      case SORT_MODE_ALPHABETICAL:
        {
          Collator collator = Collator.getInstance();
          ascendingResult = collator.compare(itemInfo1.title.toString(), itemInfo2.title.toString());
        }
        break;
      case SORT_MODE_FIRST_APPEAR:
        ascendingResult = Long.valueOf(itemInfo1.firstAppearTime).compareTo(itemInfo2.firstAppearTime);
        break;
      case SORT_MODE_CUSTOM_WITH_FIRST_APPEAR:
        ascendingResult = -(itemInfo1.priority - itemInfo2.priority);
        if (ascendingResult == 0) {
          ascendingResult = Long.valueOf(itemInfo1.firstAppearTime).compareTo(itemInfo2.firstAppearTime);
        }
        break;
      case SORT_MODE_CUSTOM_WITH_ALPHABETICAL:
        ascendingResult = -(itemInfo1.priority - itemInfo2.priority);
        if (ascendingResult == 0) {
          Collator collator = Collator.getInstance();
          ascendingResult = collator.compare(itemInfo1.title.toString(), itemInfo2.title.toString());
        }
        break;
      default:
        throw new IllegalArgumentException();
    }
    switch (flags & SORT_ORDER_MASK) {
      case SORT_ORDER_ASC:
        break;
      case SORT_ORDER_DESC:
        ascendingResult = -ascendingResult;
        break;
      default:
        throw new IllegalArgumentException();
    }

    return ascendingResult;
  }
}
