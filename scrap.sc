
class A {
    def p() = {
        println("in class A")
    }
}

class B extends A {
    override def p() = {
        super.p()
        println("in class B")
    }
}

trait T extends A {
    override def p() = {
        super.p()
        println("in trait T")
    }
}

class C extends B with T {
    override def p() = {
        super.p()
        println("in class C")
    }

}

val c = new C()
c.p()



