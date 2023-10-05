package dev.fastcampus.webfluxreactor.service

import dev.fastcampus.webfluxreactor.model.Article
import dev.fastcampus.webfluxreactor.model.CreateArticle
import dev.fastcampus.webfluxreactor.model.UpdateArticle
import dev.fastcampus.webfluxreactor.repository.ArticleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@SpringBootTest
class ArticleServiceTest(
    @Autowired private val articleService: ArticleService,
    @Autowired private val articleRepository: ArticleRepository,
    @Autowired private val transactionalOperator: TransactionalOperator,
) {

    @Test
    fun getAll() {
        Mono.zip(
            articleRepository.save(Article(title = "title 1", body = "body1", authorId = 1234)),
            articleRepository.save(Article(title = "title 2", body = "body2", authorId = 1234)),
            articleRepository.save(Article(title = "title 3", body = "body3", authorId = 1234)),
        )
            .flatMap {
                articleService.getAll("2").collectList().doOnNext {
                    assertThat(it).hasSize(1)
                }
            }
            .flatMap {
                articleService.getAll().collectList().doOnNext {
                    assertThat(it).hasSize(3)
                }
            }
            .rollback()
            .block()
    }

    @Test
    fun create() {
        articleService.create(CreateArticle(title = "title", body = "body", authorId = 1234))
            .flatMap { created ->
                articleService.get(created.id).doOnNext { get ->
                    assertThat(created.title).isEqualTo(get.title)
                    assertThat(created.body).isEqualTo(get.body)
                    assertThat(created.authorId).isEqualTo(get.authorId)
                }
            }
            .rollback()
            .block()
    }

    @Test
    fun update() {
        articleRepository.save(Article(title = "title", body = "body", authorId = 1234))
            .flatMap { new ->
                articleService.update(
                    new.id,
                    UpdateArticle(title = "update title", body = "update body", authorId = 9999)
                )
            }
            .doOnNext { update ->
                assertThat(update.title).isEqualTo("update title")
                assertThat(update.body).isEqualTo("update body")
                assertThat(update.authorId).isEqualTo(9999)
            }
            .rollback()
            .block()
    }

    @Test
    fun delete() {
        articleRepository.count()
            .flatMap { prevSize ->
                articleRepository.save(Article(title = "title", body = "body", authorId = 1234))
                    .flatMap { new ->
                        articleService.delete(new.id)
                            .doOnNext { currSize ->
                                assertThat(prevSize).isEqualTo(currSize)
                            }
                    }
            }
            .rollback()
            .block()
    }
}

class ReactiveTransactionalOperator : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        rxtx = applicationContext.getBean(TransactionalOperator::class.java)
    }

    companion object {
        lateinit var rxtx: TransactionalOperator
            private set
    }
}

fun <T> Mono<T>.rollback(): Mono<T> {
    val publisher = this
    return ReactiveTransactionalOperator.rxtx.execute { transaction ->
        transaction.setRollbackOnly()
        publisher
    }.next()
}

fun <T> Flux<T>.rollback(): Flux<T> {
    val publisher = this
    return ReactiveTransactionalOperator.rxtx.execute { transaction ->
        transaction.setRollbackOnly()
        publisher
    }
}