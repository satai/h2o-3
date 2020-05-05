import sys
sys.path.insert(1,"../../../")
import h2o
from tests import pyunit_utils
from h2o.estimators.glm import H2OGeneralizedLinearEstimator as glm

# with lambda_search=True and an alpha array and warm start, we provide a validation dataset here.
def glm_alpha_lambda_arrays():
    # compare coefficients and deviance when only training dataset is available
    train = h2o.import_file(path=pyunit_utils.locate("smalldata/glm_test/binomial_20_cols_10KRows.csv"))
    for ind in range(10):
        train[ind] = train[ind].asfactor()
    train["C21"] = train["C21"].asfactor()
    frames = train.split_frame(ratios=[0.8],seed=12345)
    d = frames[0]
    d_test = frames[1]
    regKeys = ["alphas", "lambdas", "explained_deviance_valid", "explained_deviance_train"]

    # compare results when validation dataset is present
    mLVal = glm(family='binomial',alpha=[0.1,0.5,0.9], lambda_search=True, solver='COORDINATE_DESCENT') # train with validations set
    mLVal.train(training_frame=d,x=list(range(20)),y=20, validation_frame=d_test)
    rVal = glm.getGLMRegularizationPath(mLVal)
    best_submodel_indexVal = mLVal._model_json["output"]["best_submodel_index"]
    m2Val = glm.makeGLMModel(model=mLVal,coefs=rVal['coefficients'][best_submodel_indexVal])
    dev1Val = rVal['explained_deviance_valid'][best_submodel_indexVal]
    p2Val = m2Val.model_performance(d_test)
    dev2Val = 1-p2Val.residual_deviance()/p2Val.null_deviance()
    print(dev1Val," =?= ",dev2Val)
    assert abs(dev1Val - dev2Val) < 1e-6
    orderedCoeffNames = ["C1.0","C1.1","C1.2","C1.3","C1.4","C1.5","C2.0","C2.1","C2.2","C2.3","C2.4","C2.5","C2.6",
                         "C2.7","C3.0","C3.1","C3.2","C3.3","C3.4","C3.5","C4.0","C4.1","C4.2","C4.3","C4.4","C4.5",
                         "C5.0","C5.1","C5.2","C5.3","C5.4","C6.0","C6.1","C6.2","C6.3","C6.4","C6.5","C7.0","C7.1",
                         "C7.2","C7.3","C7.4","C7.5","C7.6","C8.0","C8.1","C8.2","C8.3","C8.4","C8.5","C8.6","C8.7",
                         "C9.0","C9.1","C9.2","C9.3","C9.4","C9.5","C10.0","C10.1","C10.2","C10.3","C10.4","C10.5",
                         "C10.6","C10.7","C10.8","C11","C12","C13","C14","C15","C16","C17","C18","C19","C20",
                         "Intercept"]
    startVal = [0]*len(orderedCoeffNames)
    startVal[-1] = 0.794143412299133
    startValInit = [0]*len(orderedCoeffNames)
    startValInit[-1] = 0.794143412299133
    for l in range(0,len(rVal['lambdas'])):
        m = glm(family='binomial',alpha=[rVal['alphas'][l]],Lambda=rVal['lambdas'][l],solver='COORDINATE_DESCENT',
                startval = startVal)
        m.train(training_frame=d,x=list(range(20)),y=20, validation_frame=d_test)
        mr = glm.getGLMRegularizationPath(m)
        p = m.model_performance(d_test);
        cs = rVal['coefficients'][l]
        cs_norm = rVal['coefficients_std'][l]
        if (l+1)<len(rVal['lambdas']) and rVal['alphas'][l]!=rVal['alphas'][l+1]:
            startVal = startValInit
        else:
            startVal = pyunit_utils.extractNextCoeff(cs_norm, orderedCoeffNames, startVal) # prepare startval for next round
        pyunit_utils.assertEqualCoeffDicts(cs, m.coef(), tol=1e-1)
        pyunit_utils.assertEqualCoeffDicts(cs_norm, m.coef_norm(), tol=1e-1)
        pyunit_utils.assertEqualRegPaths(regKeys, rVal, l, mr, tol=1e-4)
        dVal = 1-p.residual_deviance()/p.null_deviance()
        if l == best_submodel_indexVal: # check training metrics, should equal for best submodel index
            pyunit_utils.assertEqualModelMetrics(m._model_json["output"]["validation_metrics"],
                                    mLVal._model_json["output"]["validation_metrics"],tol=1e-5)
        else: # for other submodel, should have worse residual_deviance() than best submodel
            assert dVal<=dev2Val, "Best submodel does not have highest explained deviance_valid for submodel: !".format(l) 

if __name__ == "__main__":
    pyunit_utils.standalone_test(glm_alpha_lambda_arrays)
else:
    glm_alpha_lambda_arrays()
