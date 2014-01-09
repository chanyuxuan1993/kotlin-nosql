package kotlin.nosql

import java.util.ArrayList

abstract class AbstractSchema(val name: String) {
    val columns = ArrayList<AbstractColumn<*, *, *>>()
}

abstract class KeyValueSchema(name: String): AbstractSchema(name) {
}

abstract class AbstractTableSchema(name: String): AbstractSchema(name) {
    val primaryKeys = ArrayList<PKColumn<*, *>>()
}

abstract class TableSchema<P>(tableName: String, primaryKey: PK<P>): AbstractTableSchema(tableName) {
    val pk = PKColumn<P, TableSchema<P>>(this, primaryKey.name, primaryKey.javaClass, primaryKey.columnType)
}

open class PK<P>(val name: jet.String, val javaClass: Class<P>, val columnType: ColumnType) {
}

fun stringPK(name: jet.String) = PK<jet.String>(name, javaClass<jet.String>(), ColumnType.STRING)
fun integerPK(name: jet.String) = PK<Int>(name, javaClass<Int>(), ColumnType.INTEGER)

val <C, T : TableSchema<C>> T.ID: AbstractColumn<C, T, C>
    get () {
        return pk as AbstractColumn<C, T, C>
    }

abstract class DocumentSchema<P, V>(name: String, val valueClass: Class<V>, primaryKey: PK<P>) : TableSchema<P>(name, primaryKey) {
}

/*
abstract class AbstractDocumentSchema<V>(name: String, val valueClass: Class<V>) : AbstractTableSchema(name) {
}
*/

fun <T: AbstractSchema> T.string(name: String): AbstractColumn<String, T, String> = AbstractColumn(this, name, javaClass<String>(), ColumnType.STRING)

fun <T: AbstractSchema> T.integer(name: String): AbstractColumn<Int, T, Int> = AbstractColumn(this, name, javaClass<Int>(), ColumnType.INTEGER)

fun <T: AbstractSchema> T.nullableString(name: String): NullableColumn<String, T> = NullableColumn(this, name, javaClass<String>(), ColumnType.STRING)

fun <T: AbstractSchema> T.nullableInteger(name: String): NullableColumn<Int, T> = NullableColumn(this, name, javaClass<Int>(), ColumnType.INTEGER)

//fun <T: AbstractSchema, C> T.setColumn(name: String, javaClass: Class<C>): SetColumn<C, T> = SetColumn(this, name, javaClass)

fun <T: AbstractSchema> T.setOfString(name: String): SetColumn<String, T> = SetColumn(this, name, javaClass<String>(), ColumnType.STRING_SET)

fun <T: AbstractSchema> T.setOfInteger(name: String): SetColumn<Int, T> = SetColumn(this, name, javaClass<Int>(), ColumnType.INTEGER_SET)

//fun <T: AbstractSchema, C> T.listColumn(name: String, javaClass: Class<C>): ListColumn<C, T> = ListColumn(this, name, javaClass)

fun <T: AbstractSchema> T.listOfString(name: String): ListColumn<String, T> = ListColumn(this, name, javaClass<String>(), ColumnType.STRING_LIST)

fun <T: AbstractSchema> T.listOfInteger(name: String): ListColumn<Int, T> = ListColumn(this, name, javaClass<Int>(), ColumnType.INTEGER_LIST)

fun <T: AbstractTableSchema> T.delete(body: T.() -> Op) {
    FilterQuery(this, body()) delete { }
}


fun <T: AbstractTableSchema, X> T.columns(selector: T.() -> X): X {
    return selector();
}

fun <T: AbstractTableSchema, B> FilterQuery<T>.map(statement: T.(Map<Any, Any>) -> B): List<B> {
    val results = ArrayList<B>()
    //Query
    return results
}

class Template1<T: AbstractTableSchema, A>(val table: T, val a: AbstractColumn<A, T, *>) {
    fun invoke(av: A): Array<Pair<AbstractColumn<*, T, *>, *>> {
        return array(Pair(a, av))
    }
}

class Quadruple<A1, A2, A3, A4>(val a1: A1, val a2: A2, val a3: A3, val a4: A4) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
}

class Quintuple<A1, A2, A3, A4, A5>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
}

fun <T: AbstractTableSchema, A, B> T.template(a: AbstractColumn<A, T, *>, b: AbstractColumn<B, T, *>): Template2<T, A, B> {
    return Template2(this, a, b)
}

class Template2<T: AbstractSchema, A, B>(val table: T, val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>) {
    /*fun invoke(av: A, bv: B): Array<Pair<Column<*, T>, *>> {
        return array(Pair(a, av), Pair(b, bv))
    }*/

    fun <C> plus(c: AbstractColumn<C, T, *>): Template3<T, A, B, C> {
        return Template3(table, a, b, c)
    }

    fun add(statement: () -> Pair<A, B>) {
        val tt = statement()
        Session.get().insert(array(Pair(a, tt.first), Pair(b, tt.second)))
    }

    /*fun values(va: A, vb: B) {
        Session.get().insert(array(Pair(a, va), Pair(b, vb)))
    }*/
}

class Template3<T: AbstractSchema, A, B, C>(val table: T, val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>, val c: AbstractColumn<C, T, *>) {
    fun invoke(av: A, bv: B, cv: C): Array<Pair<AbstractColumn<*, T, *>, *>> {
        return array(Pair(a, av), Pair(b, bv), Pair(c, cv))
    }

    /*fun invoke(): List<Quad<A, B, C, D>> {
        val results = ArrayList<Quad<A, B, C, D>>()
        Query<Quad<A, B, C, D>>(Session.get(), array(a, b, c, d)).forEach{ results.add(it) }
        return results
    }*/

    fun values(va: A, vb: B, vc: C) {
        Session.get().insert(array(Pair(a, va), Pair(b, vb), Pair(c, vc)))
    }

    fun add(statement: () -> Triple<A, B, C>) {
        val tt = statement()
        Session.get().insert(array(Pair(a, tt.component1()), Pair(b, tt.component2()), Pair(c, tt.component3())))
    }

    fun <D> plus(d: AbstractColumn<D, T, *>): Template4<T, A, B, C, D> {
        return Template4(table, a, b, c, d)
    }
}

class Template4<T: AbstractSchema, A, B, C, D>(val table: T, val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>, val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>) {
    fun invoke(av: A, bv: B, cv: C, dv: D): Array<Pair<AbstractColumn<*, T, *>, *>> {
        return array(Pair(a, av), Pair(b, bv), Pair(c, cv), Pair(d, dv))
    }

    /*fun invoke(): List<Quad<A, B, C, D>> {
        val results = ArrayList<Quad<A, B, C, D>>()
        Query<Quad<A, B, C, D>>(Session.get(), array(a, b, c, d)).forEach{ results.add(it) }
        return results
    }*/

    fun values(va: A, vb: B, vc: C, vd: D) {
        Session.get().insert(array(Pair(a, va), Pair(b, vb), Pair(c, vc), Pair(d, vd)))
    }

    fun insert(statement: () -> Quadruple<A, B, C, D>) {
        val tt = statement()
        Session.get().insert(array(Pair(a, tt.component1()), Pair(b, tt.component2()), Pair(c, tt.component3()), Pair(d, tt.component4())))
    }
}