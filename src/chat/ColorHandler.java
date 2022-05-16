package chat;

class ColorHandler {
	// color arrays
	public static String[] nameColors = { "#51b46d", // green
			"#00008b", // blue
			"#000000", // black
			"#e39e54", // orange
			"#ff0000", // red
	};

	public static String[] backgroundColors = { "#add8e6", // blue
			"#90ee90", // green
			"#ffcccb", // red
	};

	// color getters
	public static String getColor(int i) {
		return nameColors[i % nameColors.length];
	}

	public static String getBackgroundColor(int i) {
		return nameColors[i];
	}

}
