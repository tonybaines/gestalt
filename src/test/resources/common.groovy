// The root element name isn't significant
config {
  intValue = 5
  stringValue = 'Five'
  doubleValue = 5.0
  booleanValue = true
  handedness = 'left' // Don't want to import the enum definition into the config file
  getSubConfig {
    intValue = 6
  }
  strings = [
    "A",
    "B",
    "C"
  ]

  things = [
    thing {
      id = "alpha"
      stringValue = "A"
    },
    thing {
      id = "bravo"
      stringValue = "B"
    },
    thing {
      id = "charlie"
      stringValue = "C"
    }
  ]
}