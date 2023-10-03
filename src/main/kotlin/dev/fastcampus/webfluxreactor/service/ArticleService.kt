package dev.fastcampus.webfluxreactor.service

import dev.fastcampus.webfluxreactor.exception.ArticleNotFound
import dev.fastcampus.webfluxreactor.model.Article
import dev.fastcampus.webfluxreactor.model.CreateArticle
import dev.fastcampus.webfluxreactor.model.UpdateArticle
import dev.fastcampus.webfluxreactor.model.toEntity
import dev.fastcampus.webfluxreactor.repository.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
@Transactional
class ArticleService(
    private val articleRepository: ArticleRepository,
) {

    // R2DBC 는 null 을 허용 하지 않기 떄문에 null 을 리턴 할 때 다음 method chain 을 실행 시키지 않는다.
    // 그래서 switchIfEmpty 함수를 사용해 예외 처리를 함 ( findByIdOrNull 함수 없음 )
    fun get(id: Long) : Mono<Article> =
        articleRepository.findById(id)
            .switchIfEmpty { throw ArticleNotFound("No article found (id: $id)") }

    fun getAll(title: String? = null) : Flux<Article> =
        if ( title.isNullOrEmpty() ) articleRepository.findAll()
        else articleRepository.findAllByTitleContains(title)


    fun create(createArticle: CreateArticle) = articleRepository.save( createArticle.toEntity() )

    fun update(id: Long, updateArticle: UpdateArticle) =
        articleRepository.findById(id)
            .switchIfEmpty { throw ArticleNotFound("No article found (id: $id)") }
            .flatMap { article ->
                updateArticle.title?.let { article.title = it }
                updateArticle.body?.let { article.body = it }
                updateArticle.authorId?.let { article.authorId = it }
                articleRepository.save(article)
            }
    fun delete(id: Long) = articleRepository.deleteById(id)

}