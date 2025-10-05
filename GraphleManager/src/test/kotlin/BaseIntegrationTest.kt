import com.graphle.graphlemanager.GraphleManagerApplication
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [GraphleManagerApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
open class BaseIntegrationTest {
    @Autowired
    private lateinit var _mockMvc: MockMvc

    internal val mockMvc: MockMvc get() = _mockMvc

    private fun postObject(
        status: ResultMatcher = status().isOk,
        content: () -> String
    ): JsonElement {
        val response = post(status, content)
        val responseBody = response.response.contentAsString
        val obj = Json.parseToJsonElement(responseBody)
        val data = obj.jsonObject["data"]!!
        val operation = data.jsonObject.keys.first()
        val responseObj = data.jsonObject[operation]!!
        return responseObj
    }

    internal inline fun <reified T : Any> post(
        status: ResultMatcher = status().isOk,
        noinline content: () -> String
    ): T {
        val responseObj = postObject(status, content)
        val serializer = serializer(T::class.java)
        return Json.decodeFromJsonElement(serializer, responseObj) as T
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified T : Any> postListResponse(
        status: ResultMatcher = status().isOk,
        noinline content: () -> String
    ): List<T> {
        val responseObj = postObject(status, content)
        val serializer = serializer(T::class.java)
        return Json.decodeFromJsonElement(
            ListSerializer(serializer), responseObj
        ) as List<T>
    }



    internal fun post(status: ResultMatcher = status().isOk, content: () -> String) =
        mockMvc.perform(
            MockMvcRequestBuilders.post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content())
        ).andExpect(status)
            .andReturn()


    private fun globalQuery(body: () -> String): String = """{ 
        "query": "${
        body()
    }"}"""


    internal fun query(body: () -> String): String = globalQuery {
        """query { ${
            body()
                .trimIndent()
                .replace("\n", " ")
                .replace("\"", "\\\"")
        } }"""
    }

    internal fun mutation(body: () -> String): String = globalQuery {
        """mutation { ${
            body()
                .trimIndent()
                .replace("\n", " ")
                .replace("\"", "\\\"")
        } }"""
    }
}