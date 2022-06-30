import munit._

class MainSuite extends munit.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
  test("import DataFrame") {
    import org.apache.spark.sql.DataFrame
    assert(false)
  }
}
