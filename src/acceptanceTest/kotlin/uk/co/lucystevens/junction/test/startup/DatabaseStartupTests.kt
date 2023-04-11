package uk.co.lucystevens.junction.test.startup

import org.junit.jupiter.api.Test
import uk.co.lucystevens.junction.test.AcceptanceTest
import java.nio.file.Paths
import java.sql.Connection
import kotlin.io.path.readText
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// TODO this testing is probably too grnaular for acceptance tests
// we want to test more application flows than individual functions
class DatabaseStartupTests: AcceptanceTest() {

    /**
     * Tests:
     *  - testDatabaseSchemaCreated_whenNotExists
     *  - testDatabaseSchemaCreated_whenPartiallyExists
     *  - testApplicationWorks_whenSchemaAlreadyExists
     *
     *  - testEmailSaved_whenNoEmailExists
     *  - testEmailNotChanged_whenEmailAlreadyExists
     *  - testApplicationWorks_whenEmailAlreadyExistsAndNoneProvided
     *
     *  - testApplicationWorks_whenRoutesLoadedAtStartup
     *  - testApplicationWorks_whenDomainsLoadedAtStartup
     *
     */

    @Test
    fun testDatabaseSchemaCreated_whenNotExists(){
        app.run()
        validateSchema()
    }

    @Test
    fun testDatabaseSchemaCreated_whenPartiallyExists(){
        executeSqlFile("config")
        executeSqlFile("createConfig")
        app.run()
        validateSchema()

        // validate existing data retained
        queryOne("SELECT * FROM config where key='someKey'"){
            assertEquals("someValue", it.getString("value"))
        }
    }

    @Test
    fun testApplicationWorks_whenSchemaAlreadyExists(){
        executeSqlFile("routes")
        executeSqlFile("domains")
        executeSqlFile("config")
        executeSqlFile("createConfig")
        app.run()
        validateSchema()

        queryConfig().forEach { println(it) }

        // validate existing data retained
        queryOne("SELECT * FROM config where key='someKey'"){
            assertEquals("someValue", it.getString("value"))
        }
    }

    private fun validateSchema(){
        val routes = getTableColumns("routes")
        routes["host"].assertThat("varchar", true)
        routes["path"].assertThat("varchar", true)
        routes["targets"].assertThat("json", false)

        val config = getTableColumns("config")
        config["key"].assertThat("varchar", true)
        config["value"].assertThat("varchar", false)

        val domains = getTableColumns("domains")
        domains["name"].assertThat("varchar", true)
        domains["ssl"].assertThat("boolean", false)
        domains["redirectToHttps"].assertThat("boolean", false)
        domains["csr"].assertThat("varchar", false)
        domains["certificate"].assertThat("varchar", false)
        domains["keypair"].assertThat("varchar", false)
        domains["expiry"].assertThat("timestamp", false)
    }

    private fun getTableColumns(table: String) =
        query("PRAGMA table_info($table)"){ rs ->
            DatabaseColumn(
                rs.getString("name"),
                rs.getString("type"),
                rs.getBoolean("notnull")
            )
        }.associateBy { it.name }

    data class DatabaseColumn(
        val name: String,
        val type: String,
        val notNull: Boolean)

    fun DatabaseColumn?.assertThat(type: String, notNull: Boolean){
        assertNotNull(this)
        assertEquals(type, this.type, "Column $name (type)")
        assertEquals(notNull, this.notNull, "Column $name (not null)")
    }
}