(benchmark count02inc
:logic QF_BV
:extrafuns ((s0 BitVec[2]))
:extrafuns ((one BitVec[2]))
:extrafuns ((goal BitVec[2]))
:assumption (= s0 bv0[2])
:assumption (= one bv1[2])
:assumption (= goal bv3[2])
:formula (= s0 goal)
:extrafuns ((o0 Bool))
:extrafuns ((s1 BitVec[2]))
:assumption (= s1 (ite o0 (bvadd s0 one) s0))
:formula (= s1 goal)
:extrafuns ((o1 Bool))
:extrafuns ((s2 BitVec[2]))
:assumption (= s2 (ite o1 (bvadd s1 one) s1))
:formula (= s2 goal)
:extrafuns ((o2 Bool))
:extrafuns ((s3 BitVec[2]))
:assumption (= s3 (ite o2 (bvadd s2 one) s2))
:formula (= s3 goal)
)
