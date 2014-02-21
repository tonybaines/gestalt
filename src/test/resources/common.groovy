// The root element name isn't significant
config {
  intValue = 5
  stringValue = 'Five'
  doubleValue = 5.0
  booleanValue = true
  handedness = 'left' // Don't want to import the enum definition into the config file
  subConfig {
    intValue = 6
  }
  strings = [
    "A",
    "B",
    "C"
  ]

  // Not an ideal syntax here, GroovyConfig is all about Maps
  // under the covers though, so it works
  things = [
    [
      id: "alpha",
      stringValue: "A"
    ],
    [
      id: "bravo",
      stringValue: "B"
    ],
    [
      id: "charlie",
      stringValue: "C"
    ],
  ]

  propertyDefinedOnlyInGroovyConfig = 'some-value'
  propertyDefinedAllConfigSources = 'from-groovy-config'
  declaredAsAnIntegerButIsAString = 'Whoops!'

  // GroovyConfig is really a Map syntax, so last one wins
  somethingDefinedTwice = 'Foo'
  somethingDefinedTwice = 'Bar'

  integerThatIsTooLarge = 11
}