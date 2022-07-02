import munit._

class MainSuite extends munit.FunSuite {
  test("Visibility of Test code") {
    val obtained = 42
    val expected = Utility.value
    assertEquals(obtained, expected)
  }
  test("ability to import Compile dependencies") {
    import org.apache.spark.sql.DataFrame
    assert(false)
  }
}
