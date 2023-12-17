package tec.ac.cr.marape.app

import me.xdrop.fuzzywuzzy.FuzzySearch
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FuzzySearchUnitTest {
    @Test
    fun `fuzzy search all lowercase`() {
      assert(FuzzySearch.ratio("hola", "hola") == 100)
    }

  @Test
  fun `fuzzy search must be equal to 50`() {
    assert(FuzzySearch.ratio("hola", "hopp") == 50)
  }

  @Test
  fun `fuzzy search must be greater than 30`() {
    assert(FuzzySearch.ratio("ya va", "yava") > 30)
  }

  @Test
  fun `fuzzy search must be equal to 0`() {
    assert(FuzzySearch.ratio("", "hola") == 0)
  }
}