package trotech.service.usecase.math

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.EigenDecomposition
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.alg.connectivity.ConnectivityInspector

/**
 * Показывает, склонны ли высокоактивные узлы соединяться с такими же.
 *
 * В p2p-сетях отрицательная ассортативность может быть полезна:
 *
 *      высокостепенные узлы (суперноды) связываются с низкоуровневыми для устойчивости.
 *
 *      положительная ассортативность = возможные кластеры → возможные SPOF'ы.
 */

fun assortativityCoefficient(graph: Graph<String, DefaultEdge>): Double {
    if (graph.edgeSet().size == 0 || graph.vertexSet().size == 0) {
        return 0.0
    }
    val degrees = graph.vertexSet().associateWith { graph.degreeOf(it) }
    val edges = graph.edgeSet()

    val jkList = edges.map {
        val v1 = graph.getEdgeSource(it)
        val v2 = graph.getEdgeTarget(it)
        val d1 = degrees[v1]!!
        val d2 = degrees[v2]!!
        d1 to d2
    }

    val m = jkList.size.toDouble()
    val jkMean = jkList.map { it.first * it.second }.sum() / m
    val jMean = jkList.map { (j, _) -> j }.average()
    val kMean = jkList.map { (_, k) -> k }.average()
    val numerator = jkMean - jMean * kMean
    val jSqMean = jkList.map { (j, _) -> j * j }.average()
    val kSqMean = jkList.map { (_, k) -> k * k }.average()
    val denominator = 0.5 * (jSqMean + kSqMean) - jMean * kMean
    
    return if (denominator == 0.0) 0.0 else numerator / denominator
}

/**
 * Показывает, насколько легко «разрезать» сеть.
 *
 * Чем ближе к 0 — тем уязвимее сеть к фрагментации.
 */

fun computeAlgebraicConnectivity(graph: Graph<String, DefaultEdge>): Double {
    if (graph.edgeSet().size == 0 || graph.vertexSet().size == 0) {
        return 0.0
    }
    val vertices = graph.vertexSet().toList()
    val n = vertices.size
    val adjacency = Array(n) { DoubleArray(n) }

    // Заполняем матрицу смежности
    for (i in 0 until n) {
        for (j in 0 until n) {
            if (graph.containsEdge(vertices[i], vertices[j]) ||
                graph.containsEdge(vertices[j], vertices[i])) {
                adjacency[i][j] = 1.0
            }
        }
    }

    // Матрица степеней
    val degreeMatrix = Array(n) { DoubleArray(n) }
    for (i in 0 until n) {
        degreeMatrix[i][i] = adjacency[i].sum()
    }

    // Лапласиан: L = D - A
    val laplacian = Array2DRowRealMatrix(n, n)
    for (i in 0 until n) {
        for (j in 0 until n) {
            laplacian.setEntry(i, j, degreeMatrix[i][j] - adjacency[i][j])
        }
    }

    // Спектральный анализ
    val eigenDecomp = EigenDecomposition(laplacian)
    val eigenvalues = eigenDecomp.realEigenvalues.sorted()
    return if (eigenvalues.size < 2) 0.0 else eigenvalues[1] // λ2 — алгебраическая связность
}

/**
 *  Kotlin-функция для вычисления локального и глобального коэффициента кластеризации в неориентированном графе.
 *  Измеряет, насколько сильно узлы "скучены" — важен для выявления:
 *
 *      групп френдов (локальность),
 *
 *      повторяющихся подключений (redundancy),
 *
 *      потенциальных точек перегрева (циклы).
 */

fun <V> clusteringCoefficient(graph: Graph<V, DefaultEdge>): Pair<Double, Map<V, Double>> {
    val localCoefficients = mutableMapOf<V, Double>()
    var totalTriangles = 0.0
    var totalTriplets = 0.0

    for (v in graph.vertexSet()) {
        val neighbors = graph.edgesOf(v).map {
            val src = graph.getEdgeSource(it)
            val tgt = graph.getEdgeTarget(it)
            if (src == v) tgt else src
        }.toSet()

        val degree = neighbors.size
        if (degree < 2) {
            localCoefficients[v] = 0.0
            continue
        }

        // Считаем количество связей между соседями (т.е. замкнутые треугольники)
        var linksBetweenNeighbors = 0
        val neighborList = neighbors.toList()
        for (i in 0 until neighborList.size) {
            for (j in i + 1 until neighborList.size) {
                if (graph.containsEdge(neighborList[i], neighborList[j]) ||
                    graph.containsEdge(neighborList[j], neighborList[i])) {
                    linksBetweenNeighbors++
                }
            }
        }

        val possibleLinks = degree * (degree - 1) / 2.0
        val localClustering = linksBetweenNeighbors / possibleLinks
        localCoefficients[v] = localClustering

        totalTriangles += linksBetweenNeighbors
        totalTriplets += possibleLinks
    }

    val globalClustering = if (totalTriplets == 0.0) 0.0 else totalTriangles / totalTriplets
    return Pair(globalClustering, localCoefficients)
}


/**
 * Функция для вычисления компонент связности в графе.
 * Возвращает список компонент, каждая из которых представляет собой множество вершин.
 */
fun <V> connectedComponents(graph: Graph<V, DefaultEdge>): List<Set<V>> {
    val inspector = ConnectivityInspector(graph)
    return inspector.connectedSets() // Возвращает список компонент
}