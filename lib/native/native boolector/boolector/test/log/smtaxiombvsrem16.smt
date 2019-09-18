(benchmark smtaxiombvsrem
 :logic QF_BV
 :extrafuns ((s BitVec[16]))
 :extrafuns ((t BitVec[16]))
 :formula (not (=
    (bvsrem s t) 
      (let (?msb_s (extract[15:15] s))
      (let (?msb_t (extract[15:15] t))
      (ite (and (= ?msb_s bit0) (= ?msb_t bit0))
           (bvurem s t)
      (ite (and (= ?msb_s bit1) (= ?msb_t bit0))
           (bvneg (bvurem (bvneg s) t))
      (ite (and (= ?msb_s bit0) (= ?msb_t bit1))
           (bvurem s (bvneg t))
           (bvneg (bvurem (bvneg s) (bvneg t))))))))
)))
