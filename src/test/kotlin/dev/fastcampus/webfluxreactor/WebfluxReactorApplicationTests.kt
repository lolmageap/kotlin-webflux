package dev.fastcampus.webfluxreactor

import dev.fastcampus.webfluxreactor.model.Article
import dev.fastcampus.webfluxreactor.repository.ArticleRepository
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

private val logger = KotlinLogging.logger {}

@SpringBootTest
class WebfluxReactorApplicationTests(
	@Autowired private val articleRepository: ArticleRepository
) {
	@Test
	fun contextLoads() {
		val count = articleRepository.count().block() ?: 0
		articleRepository.save( Article(title = "title") ).block()
		val all = articleRepository.findAll().collectList().block()
		all?.forEach { logger.debug { it } }

		val currentCount = articleRepository.count().block() ?: 0
		assertThat(currentCount).isEqualTo(count + 1)
	}

}
