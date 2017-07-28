# Install script for directory: /home/falthiel/Downloads/g2o/g2o/core

# Set the install prefix
IF(NOT DEFINED CMAKE_INSTALL_PREFIX)
  SET(CMAKE_INSTALL_PREFIX "/home/falthiel/Downloads/android-toolchain/user")
ENDIF(NOT DEFINED CMAKE_INSTALL_PREFIX)
STRING(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
IF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  IF(BUILD_TYPE)
    STRING(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  ELSE(BUILD_TYPE)
    SET(CMAKE_INSTALL_CONFIG_NAME "Release")
  ENDIF(BUILD_TYPE)
  MESSAGE(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
ENDIF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)

# Set the component getting installed.
IF(NOT CMAKE_INSTALL_COMPONENT)
  IF(COMPONENT)
    MESSAGE(STATUS "Install component: \"${COMPONENT}\"")
    SET(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  ELSE(COMPONENT)
    SET(CMAKE_INSTALL_COMPONENT)
  ENDIF(COMPONENT)
ENDIF(NOT CMAKE_INSTALL_COMPONENT)

# Install shared libraries without execute permission?
IF(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  SET(CMAKE_INSTALL_SO_NO_EXE "1")
ENDIF(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  list(APPEND CMAKE_ABSOLUTE_DESTINATION_FILES
   "/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_core.so")
  IF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(WARNING "ABSOLUTE path INSTALL DESTINATION : ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
  IF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(FATAL_ERROR "ABSOLUTE path INSTALL DESTINATION forbidden (by caller): ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
FILE(INSTALL DESTINATION "/home/falthiel/Downloads/android-toolchain/user/lib" TYPE SHARED_LIBRARY FILES "/home/falthiel/Downloads/g2o/androidbuild/g2o/core/CMakeFiles/CMakeRelink.dir/libg2o_core.so")
  IF(EXISTS "$ENV{DESTDIR}/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_core.so" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_core.so")
    IF(CMAKE_INSTALL_DO_STRIP)
      EXECUTE_PROCESS(COMMAND "/home/falthiel/Downloads/android-toolchain/bin/arm-linux-androideabi-strip" "$ENV{DESTDIR}/home/falthiel/Downloads/android-toolchain/user/lib/libg2o_core.so")
    ENDIF(CMAKE_INSTALL_DO_STRIP)
  ENDIF()
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  list(APPEND CMAKE_ABSOLUTE_DESTINATION_FILES
   "/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm_gauss_newton.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/linear_solver.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/hyper_graph.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_vertex.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/estimate_propagator.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/block_solver.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimizable_graph.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/parameter.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/robust_kernel_factory.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/sparse_block_matrix_diagonal.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/robust_kernel.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/sparse_block_matrix.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm_property.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm_levenberg.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/g2o_core_api.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/marginal_covariance_cholesky.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/robust_kernel_impl.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/solver.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/parameter_container.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/sparse_block_matrix_ccs.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/jacobian_workspace.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm_dogleg.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/cache.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/hyper_graph_action.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/sparse_optimizer_terminate_action.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_multi_edge.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_edge.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/batch_stats.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm_with_hessian.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/sparse_optimizer.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/matrix_structure.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/matrix_operations.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_binary_edge.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/openmp_mutex.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/factory.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_unary_edge.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/optimization_algorithm_factory.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/creators.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/hyper_dijkstra.h;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/sparse_block_matrix.hpp;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_multi_edge.hpp;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_vertex.hpp;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_binary_edge.hpp;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/base_unary_edge.hpp;/home/falthiel/Downloads/android-toolchain/user/include/g2o/core/block_solver.hpp")
  IF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(WARNING "ABSOLUTE path INSTALL DESTINATION : ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION)
  IF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
    message(FATAL_ERROR "ABSOLUTE path INSTALL DESTINATION forbidden (by caller): ${CMAKE_ABSOLUTE_DESTINATION_FILES}")
  ENDIF (CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION)
FILE(INSTALL DESTINATION "/home/falthiel/Downloads/android-toolchain/user/include/g2o/core" TYPE FILE FILES
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm_gauss_newton.h"
    "/home/falthiel/Downloads/g2o/g2o/core/linear_solver.h"
    "/home/falthiel/Downloads/g2o/g2o/core/hyper_graph.h"
    "/home/falthiel/Downloads/g2o/g2o/core/base_vertex.h"
    "/home/falthiel/Downloads/g2o/g2o/core/estimate_propagator.h"
    "/home/falthiel/Downloads/g2o/g2o/core/block_solver.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimizable_graph.h"
    "/home/falthiel/Downloads/g2o/g2o/core/parameter.h"
    "/home/falthiel/Downloads/g2o/g2o/core/robust_kernel_factory.h"
    "/home/falthiel/Downloads/g2o/g2o/core/sparse_block_matrix_diagonal.h"
    "/home/falthiel/Downloads/g2o/g2o/core/robust_kernel.h"
    "/home/falthiel/Downloads/g2o/g2o/core/sparse_block_matrix.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm_property.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm_levenberg.h"
    "/home/falthiel/Downloads/g2o/g2o/core/g2o_core_api.h"
    "/home/falthiel/Downloads/g2o/g2o/core/marginal_covariance_cholesky.h"
    "/home/falthiel/Downloads/g2o/g2o/core/robust_kernel_impl.h"
    "/home/falthiel/Downloads/g2o/g2o/core/solver.h"
    "/home/falthiel/Downloads/g2o/g2o/core/parameter_container.h"
    "/home/falthiel/Downloads/g2o/g2o/core/sparse_block_matrix_ccs.h"
    "/home/falthiel/Downloads/g2o/g2o/core/jacobian_workspace.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm_dogleg.h"
    "/home/falthiel/Downloads/g2o/g2o/core/cache.h"
    "/home/falthiel/Downloads/g2o/g2o/core/hyper_graph_action.h"
    "/home/falthiel/Downloads/g2o/g2o/core/sparse_optimizer_terminate_action.h"
    "/home/falthiel/Downloads/g2o/g2o/core/base_multi_edge.h"
    "/home/falthiel/Downloads/g2o/g2o/core/base_edge.h"
    "/home/falthiel/Downloads/g2o/g2o/core/batch_stats.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm_with_hessian.h"
    "/home/falthiel/Downloads/g2o/g2o/core/sparse_optimizer.h"
    "/home/falthiel/Downloads/g2o/g2o/core/matrix_structure.h"
    "/home/falthiel/Downloads/g2o/g2o/core/matrix_operations.h"
    "/home/falthiel/Downloads/g2o/g2o/core/base_binary_edge.h"
    "/home/falthiel/Downloads/g2o/g2o/core/openmp_mutex.h"
    "/home/falthiel/Downloads/g2o/g2o/core/factory.h"
    "/home/falthiel/Downloads/g2o/g2o/core/base_unary_edge.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm.h"
    "/home/falthiel/Downloads/g2o/g2o/core/optimization_algorithm_factory.h"
    "/home/falthiel/Downloads/g2o/g2o/core/creators.h"
    "/home/falthiel/Downloads/g2o/g2o/core/hyper_dijkstra.h"
    "/home/falthiel/Downloads/g2o/g2o/core/sparse_block_matrix.hpp"
    "/home/falthiel/Downloads/g2o/g2o/core/base_multi_edge.hpp"
    "/home/falthiel/Downloads/g2o/g2o/core/base_vertex.hpp"
    "/home/falthiel/Downloads/g2o/g2o/core/base_binary_edge.hpp"
    "/home/falthiel/Downloads/g2o/g2o/core/base_unary_edge.hpp"
    "/home/falthiel/Downloads/g2o/g2o/core/block_solver.hpp"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

