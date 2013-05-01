'use strict';

/* Controllers */

angular.module('dashboard.controllers', [])
    .controller('MarketingBuild', ['$scope', function ($scope) {
        $scope.clauses = [
            {}
        ];
        $scope.createCampaign = function (err) {
            alert(err);

        }

        $scope.createCampaign = function (data) {
        }

        $scope.createCampaign = function () {
            alert('test');
            //$scope.customer.$save(_fn_success_put_post, _fn_error);
        }
        $scope.createClause = function () {
            $scope.clauses.push({});
        }
    }])
    .controller('MyCtrl2', [function () {

    }]);