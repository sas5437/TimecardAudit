package timecard;

import java.util.Comparator;

public class TimePairComparator implements Comparator<TimePair> {
  @Override
  public int compare(TimePair a, TimePair b) {
    return a.getClockIn().compareTo(b.getClockIn());
  }
}
