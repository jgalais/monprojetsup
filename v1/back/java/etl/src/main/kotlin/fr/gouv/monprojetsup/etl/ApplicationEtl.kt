package fr.gouv.monprojetsup.etl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApplicationEtl

fun main(args: Array<String>) {
	runApplication<ApplicationEtl>(*args)
}
