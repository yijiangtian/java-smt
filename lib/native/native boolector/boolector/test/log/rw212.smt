(benchmark fuzzsmt
:logic QF_BV
:status unknown
:extrafuns ((v0 BitVec[16]))
:extrafuns ((v1 BitVec[7]))
:extrafuns ((v2 BitVec[15]))
:formula
(let (?e3 bv31209[16])
(let (?e4 (ite (bvsle v2 v2) bv1[1] bv0[1]))
(let (?e5 (ite (bvsge (sign_extend[15] ?e4) v0) bv1[1] bv0[1]))
(let (?e6 (ite (bvugt (sign_extend[6] ?e5) v1) bv1[1] bv0[1]))
(let (?e7 (bvudiv ?e6 ?e6))
(let (?e8 (ite (bvsgt ?e4 ?e7) bv1[1] bv0[1]))
(let (?e9 (ite (bvsle ?e7 ?e4) bv1[1] bv0[1]))
(let (?e10 (sign_extend[1] ?e7))
(let (?e11 (ite (distinct ?e3 (zero_extend[15] ?e5)) bv1[1] bv0[1]))
(flet ($e12 (distinct ?e9 ?e11))
(flet ($e13 (bvuge ?e5 ?e6))
(flet ($e14 (distinct v2 (zero_extend[14] ?e9)))
(flet ($e15 (= v0 (sign_extend[15] ?e4)))
(flet ($e16 (bvugt ?e5 ?e5))
(flet ($e17 (bvule (sign_extend[15] ?e4) v0))
(flet ($e18 (bvsle (zero_extend[6] ?e4) v1))
(flet ($e19 (bvsgt ?e6 ?e6))
(flet ($e20 (bvsle ?e5 ?e7))
(flet ($e21 (bvslt ?e9 ?e7))
(flet ($e22 (bvult (zero_extend[14] ?e9) v2))
(flet ($e23 (bvugt v1 (zero_extend[6] ?e11)))
(flet ($e24 (bvsle v0 (sign_extend[15] ?e6)))
(flet ($e25 (bvsle ?e6 ?e4))
(flet ($e26 (bvuge (sign_extend[8] v1) v2))
(flet ($e27 (bvuge (sign_extend[15] ?e6) v0))
(flet ($e28 (bvugt v0 (zero_extend[15] ?e6)))
(flet ($e29 (distinct ?e3 (zero_extend[15] ?e6)))
(flet ($e30 (= v2 (zero_extend[14] ?e9)))
(flet ($e31 (bvsgt (zero_extend[1] ?e4) ?e10))
(flet ($e32 (bvugt ?e11 ?e5))
(flet ($e33 (bvule ?e4 ?e7))
(flet ($e34 (bvult ?e5 ?e6))
(flet ($e35 (bvslt v1 (sign_extend[6] ?e11)))
(flet ($e36 (bvslt ?e9 ?e7))
(flet ($e37 (bvule v2 v2))
(flet ($e38 (bvuge ?e3 (zero_extend[15] ?e4)))
(flet ($e39 (bvsle v1 (zero_extend[6] ?e5)))
(flet ($e40 (bvslt ?e10 (sign_extend[1] ?e5)))
(flet ($e41 (bvslt (sign_extend[6] ?e8) v1))
(flet ($e42 (implies $e41 $e33))
(flet ($e43 (and $e37 $e26))
(flet ($e44 (or $e32 $e36))
(flet ($e45 (iff $e24 $e43))
(flet ($e46 (or $e38 $e14))
(flet ($e47 (iff $e19 $e22))
(flet ($e48 (and $e25 $e29))
(flet ($e49 (not $e27))
(flet ($e50 (and $e21 $e35))
(flet ($e51 (xor $e47 $e31))
(flet ($e52 (if_then_else $e49 $e40 $e50))
(flet ($e53 (xor $e12 $e51))
(flet ($e54 (if_then_else $e28 $e34 $e53))
(flet ($e55 (xor $e42 $e18))
(flet ($e56 (not $e52))
(flet ($e57 (if_then_else $e54 $e30 $e54))
(flet ($e58 (and $e48 $e45))
(flet ($e59 (xor $e56 $e44))
(flet ($e60 (not $e20))
(flet ($e61 (if_then_else $e60 $e15 $e39))
(flet ($e62 (or $e58 $e46))
(flet ($e63 (or $e62 $e57))
(flet ($e64 (or $e55 $e63))
(flet ($e65 (xor $e61 $e61))
(flet ($e66 (implies $e65 $e64))
(flet ($e67 (xor $e13 $e17))
(flet ($e68 (iff $e59 $e66))
(flet ($e69 (if_then_else $e16 $e23 $e68))
(flet ($e70 (not $e67))
(flet ($e71 (not $e69))
(flet ($e72 (not $e71))
(flet ($e73 (implies $e70 $e70))
(flet ($e74 (and $e73 $e72))
(flet ($e75 (and $e74 (not (= ?e6 bv0[1]))))
$e75
))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))

