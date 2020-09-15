package app.argparse;

public class Switch {

	private String shortFlag = null, longFlag = null;

	public Switch(String shortFlag, String longFlag) {
		this.shortFlag = shortFlag;
		this.longFlag = longFlag;
	}

	public String getShort() { return shortFlag; }

	public String getLong() { return longFlag; }

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Switch)) return false;
		else {
			Switch f = (Switch) o;
			return shortFlag.equals(f.shortFlag) && longFlag.equals(f.longFlag);
		}
	}

}
