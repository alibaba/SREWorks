module.exports = {
  roots: ['<rootDir>/src/'],
  transform: {
    '^.+\\.jsx?$': 'babel-jest',
  },
  testRegex: '.(test|spec).(jsx?)$',
  moduleFileExtensions: ['js', 'jsx', 'json', 'node'],
  collectCoverage: true,
  coveragePathIgnorePatterns: ['.mock.(jsx?)$'],
  verbose: true,
}
