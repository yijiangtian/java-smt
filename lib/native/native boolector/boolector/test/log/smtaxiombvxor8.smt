(benchmark smtaxiombvxor
 :logic QF_BV
 :extrafuns ((s BitVec[8]))
 :extrafuns ((t BitVec[8]))
 :formula (not (=
    (bvxor s t)  (bvor (bvand s (bvnot t)) (bvand (bvnot s) t))
)))
