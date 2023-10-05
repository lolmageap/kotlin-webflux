package dev.fastcampus.webfluxreactor.controller

import dev.fastcampus.webfluxreactor.model.Article
import dev.fastcampus.webfluxreactor.model.CreateArticle
import dev.fastcampus.webfluxreactor.repository.ArticleRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.temporal.ChronoUnit

@SpringBootTest
class ArticleControllerTest(
    @Autowired private val applicationContext: ApplicationContext,
    @Autowired private val articleRepository: ArticleRepository,
) {

    val client = WebTestClient.bindToApplicationContext(applicationContext).build()

    @AfterEach
    fun tearDown() {
        articleRepository.deleteAll()
    }

    @Test
    fun create() {
        val request = CreateArticle(
            title = "title 1",
            body = "body 1",
            authorId = 1111
        )

        client.post().uri("/articles")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("title").isEqualTo(request.title)
            .jsonPath("body").isEqualTo(request.body!!)
            .jsonPath("authorId").isEqualTo(request.authorId!!)

    }

    @Test
    fun update() {
        val createRequest = CreateArticle(
            title = "title 1",
            body = "body 1",
            authorId = 1111
        )
        val updateRequest = CreateArticle(
            title = "title update",
            body = "body update",
            authorId = 1234
        )

        val created = client.post().uri("/articles")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(createRequest)
            .exchange()
            .expectBody(Article::class.java)
            .returnResult()
            .responseBody!!

        val update = client.put().uri("/articles/${created.id}")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(Article::class.java)
            .returnResult()
            .responseBody!!

        assertThat(update.title).isEqualTo(updateRequest.title)
        assertThat(update.body).isEqualTo(updateRequest.body)
        assertThat(update.authorId).isEqualTo(updateRequest.authorId)
    }

    @Test
    fun delete() {
        val request = CreateArticle(
            title = "title 1",
            body = "body 1",
            authorId = 1111
        )

        val created = client.post().uri("/articles")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectBody(Article::class.java)
            .returnResult()
            .responseBody!!

        client.delete().uri("/articles/$created.id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated
            .expectBody()

    }

    @Test
    fun get() {
        val request = CreateArticle(
            title = "title 1",
            body = "body 1",
            authorId = 1111
        )

        val created = client.post().uri("/articles")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectBody(Article::class.java)
            .returnResult()
            .responseBody!!

        val read = client.get().uri("/articles/${created.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Article::class.java)
            .returnResult()
            .responseBody!!

        assertThat(created.title).isEqualTo(read.title)
        assertThat(created.body).isEqualTo(read.body)
        assertThat(created.authorId).isEqualTo(read.authorId)

        assertThat(created.createdAt?.truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(read.createdAt?.truncatedTo(ChronoUnit.MILLIS))

        assertThat(created.updatedAt?.truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(read.updatedAt?.truncatedTo(ChronoUnit.MILLIS))
    }

    @Test
    fun getAll() {
        repeat(5) { i ->
            val request = CreateArticle(title = "title $i", body = "body $i", authorId = i.toLong())
            client.post().uri("/articles")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectBody(Article::class.java)
                .returnResult()
                .responseBody!!
        }

        client.post().uri("/articles")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(
                CreateArticle(title = "title matched", body = "body matched", authorId = 9999)
            )
            .exchange()
            .expectBody(Article::class.java)
            .returnResult()
            .responseBody!!

        client.get().uri("/articles?title=matched")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)

        client.get().uri("/articles")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(5)

    }
}