package geo

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}

class PolylineSpec extends FlatSpec with Matchers with LazyLogging {

    val example = List(LatLng(38.5, -120.2), LatLng(40.7, -120.95), LatLng(43.252, -126.453))

    "A Polyline" should "decode a String into a List of LatLng" in {
        val str = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
        val coordinates = Polyline.decode(str)
        coordinates should be(example)
    }

    it should "decode an empty String into an empty List" in {
        Polyline.decode("") should be(List[LatLng]())
    }

    it should "decode a String with a custom precision into a List of LatLng" in {
        val str = "_izlhA~rlgdF_{geC~ywl@_kwzCn`{nI"
        val coordinates = Polyline.decode(str, 6)
        coordinates should be(example)
    }

    it should "encode a List of LatLng into a String" in {
        val str = Polyline.encode(example)
        "_p~iF~ps|U_ulLnnqC_mqNvxq`@" should be(str)
    }

    it should "encode an empty List of LatLng into an empty String" in {
        Polyline.encode(List[LatLng]()) should be("")
    }

    it should "encode a List of LatLng and precision into a String" in {
        val str = Polyline.encode(example, 6)
        "_izlhA~rlgdF_{geC~ywl@_kwzCn`{nI" should be(str)
    }

    /* distance tests */
    it should "calculate the distance from an encoded string" in {
        val str = "m~csH_n}X{HmGe@_@e@YSMYQUKsGcEcBgAuDcC[OWMa@Q_@Qu@Ou@EcB@QEKEWOOIoDiA"
        val pl = Polyline(str)
        958 should be(Math.floor(pl.distance()))
    }

    it should "calculate the distance from a List of LatLng" in {
        val pl = Polyline(List[LatLng](
            LatLng(52.516272, 13.377722), // Berlin
            LatLng(51.515, 7.453619), // Dortmund
            LatLng(51.503333, -0.119722) // London
        ))
        945235 should be(Math.floor(pl.distance()))
    }

    it should "calculate a point at a given distance 1" in {
        val str = "yl~rHyplZz@pFgCdG_C|D}EpEgAhL|AzNFrBAx@}@bCaJs@wFw@{@a@i@XBdAPPzC`N~TvjA~WnuAjJxe@hIn_@dBxEf@nGkAjTw@vQwChOmMv]sNj\\kIpN_JdIeJdDwFb@}FIkPeByCm@iDyAmCkDiFsIcE{BuDPuD|CeBfF_@nFDpCx@hF~HnPv@`FLlDIpP]lJ_D~f@oAzPuV|lD{JhvAqAf\\OxOf@j^`FhsAlEdiAtFzbBDl`@cApZkAxNsChSoGfYaMj_@sMb\\mWzl@kSzi@qOfg@qFrUcEnVwBbSkAdS[hQR`U|Ab[nAf[Kf\\wGjgAuSj~CuAf\\wA|n@P|V~A~W|B`PtGl]rIpq@vQ`cB`Jjv@tIbo@jOvgArRfvA``@tpC~DbZnAhSMzY{B`Vmd@|hDg\\leCkAzRSpQj@rUfBxRjCzOpDfOfIhXfJz\\bErTrDxXvB~WfAdZtAl_@pBzQ|DhQ|HzRfJpMzM|JfL~Eta@zOjIxEtHzFvKvK|MpRrItP|EdM|FnRrFfX`BbLbDj_@z@j[lAxcEpB`_Fr@dqBXbj@lAfs@l@njAf@ptAPdg@ItG[`XaAxKgBxJ{ElOkHxLgG~FgKrFkP|G}dAbc@cJvEgIvGyHjKqEnJgFbQaLbq@}e@pzC{FlZsKzc@mEbOwEtLeGbKkF|FaI~Fke@lV{H~FsGdH}FbKcE~LuClN_Ef^sMb~AyDrY_E`TeQzs@qL~d@wIbWkMtVkXr]{PzWcGjMkGfQkJh`@oCbQ}Ipy@eShmBoJ|}@cVd}BgNjnAeJvl@gL~h@uLvb@cRdh@yLhX{J`R{OzT{QnPkOzI}KzEg]dM{StI}MbJcTzSsTzVyKnRmI~TuGdYuGjc@qIrdAoBfPyBtKkDvK}B~E}DrGaGhGcGlE}SdKyd@tSmYpL_MjDeMjC}SbCeH`B_DtAaH~FqGbKkEhKiCdImEbTgCxV}@jSi@zc@Ehy@l@l^dBl[nCnWxF`]hFtTbNxd@fObe@rW`y@jY``AtHxZvArJNlDKlFoGn[yChGmB~BuWbUuXxTq|@hm@_WdSsvAfjAsK`I_JlEqHtBcKlAaQQec@wF}i@gI}[wE_X_Ba^yCcYwEmNeDiYoImZsIgNcBaJEmKx@}f@fJw{A|YuPfD_JjAsHKqHyAea@}MiKoBmO[cHh@kIdByGjCaJhFuErDeIdJqL`S_GbN{JrXmPlc@gP`^eTfj@mw@paCqE`NmDxHeGtMcDjB}QKmA_@_B{AyDgH"
        val pl = Polyline(str)
        val target = pl.pointAtDistance(50)
        target.lat should be (50.459646999543196 +- 0.000009)
        target.lng should be (4.492432564824185 +- 0.000009)
    }

    it should "calculate a point at a given distance 2" in {
        val icelandRouteString = "ga|iKbi`_CdHbxJ_MvTuP||@qJ`lJ}VnaKlP~eOxRt{Fmg@~{BpQddKlZhqCmw@kUkt@wdAga@pyBwWlf@k{AkoBwdAjnAoz@qaD{ZtWggBcHwcAjSglBugAa{BwfHih@ooH{CmdDkfCg}Gk}CggByhBcsAkf@anE{sBanK{g@efCme@aXopA|d@ifAji@e}AyeAm~A|nAcoCp{@meFvuCqr@tt@mz@ki@u~AyzA}hBvp@qrBqz@wkBeu@id@ctBygAmResBzKo}BtIowCjfAe|ChcB_{@vIe_Amq@s`BieGoiCscOmqAwoGcaA_gB{nAQgm@ciDzc@wkEdMstIwlAy|Rmi@mbDaeA{`AmlBF{yAkgAkgBivDcp@o`AgmDk|H{}B_sO_gDgzQsdAgpCur@nXakCb[euMy~GwfEk~AqZgmBud@qsA`b@kvEd}BqvI~aEkzFz`BquE|{Fo{[`cEg~O{OmmIzuAm}FbJs{Kg{@g~DvUwrMkoAgaKqjBmbEeOqkBbKykEym@tA`LqcOvLimCtsAaaBzzCuoDnhCmy@vyDm_HhkCydKhw@y~SmiCwaNg}AieX}t@eyd@aqFevKmaF{yHcvDidAwzCwqE_yDcdEknC}_FgjAkoCq{Aaw@_jFixNusC_fJa^g~F`SwdDx`CwvC`vDcxJdg@_xBvmBisBnBq~EqsApN}eGvsCi|CvDscIkzBc`AgzByDctCtIqxChcDicKn`BisAxmCrK`nCyhT|lAgfPfbDmgd@myCmyH}jBwtDmQo_Dqg@udAbzBavFtqE{hCttC_vIxeCajEtaBesAdO}iCrsBssCl~@_`Frb@oqCmq@wqB{vAqfEkx@_hGuTubCcz@mkAshCmiCeo@sgBzz@e_Hh`A{tBhAgcGaL}mEeSwgFmZm{JrGcgUykAkgN|\\i_PjpBwr_@jpBksOag@wlEnqBaxC|oB__IjzCkdL~lAgoEpmBch@fcFqpBvcDwrChkCzj@xi@ggC_BirFsiAi}Now@gwQrm@czZiYwgJvp@msCtjBo{@haBenG`kFaqAxoGepGvuE}nNpHcbNkgC{kYuaCixN{_B_fV__Cy~\\giBu_Mc|AyoFaoBucCdMslBhiAeg@j[etDbkAm`B`jDcx@p_AsYpeAipClsEydEnfBwgAfkAi~CpwAk}BzmA{ZtfA`}CzeA|gBpuCfbD`jCbaCr~@l~@jvBpy@buDh\\laB~iGnoAzjBhuA`Cp{B{qAxdAhe@pwA`^npAnpBnbA_BtiDhlAzd@fa@"
        val pl = Polyline(icelandRouteString)
        val target = pl.pointAtDistance(14600)
        target.lat should be (64.7064416238049 +- 0.000009)
        target.lng should be (-21.278854085036983 +- 0.000009)
    }

}
