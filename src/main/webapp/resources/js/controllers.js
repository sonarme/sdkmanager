'use strict';

/* Controllers */

angular.module('dashboard.controllers', [])
    .controller('MarketingBuild', ['$scope', 'Campaign', function ($scope, Campaign) {
        $scope.clauses = [
            {}
        ];
        function _fn_error(err) {
            alert(err);

        }

        function _fn_success_put_post(data) {
        }

        $scope.save = function () {
            var campaign = new Campaign($scope.campaign);
            campaign.$save();
        };

        $scope.createClause = function () {
            $scope.clauses.push({});
        }
    }])
    .controller('GeofenceBuildCtrl', [function () {

    }]);